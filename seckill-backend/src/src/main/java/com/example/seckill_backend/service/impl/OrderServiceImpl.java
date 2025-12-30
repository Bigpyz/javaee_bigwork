package com.example.seckill_backend.service.impl;

import com.example.seckill_backend.common.AuthContext;
import com.example.seckill_backend.common.BizException;
import com.example.seckill_backend.common.ErrorCode;
import com.example.seckill_backend.mapper.OrderMapper;
import com.example.seckill_backend.mapper.ActivityProductMapper;
import com.example.seckill_backend.mapper.UserMapper;

import com.example.seckill_backend.model.Activity;
import com.example.seckill_backend.model.Order;
import com.example.seckill_backend.model.ActivityProduct;
import com.example.seckill_backend.model.User;
import com.example.seckill_backend.service.CaptchaService;
import com.example.seckill_backend.service.OrderService;
import com.example.seckill_backend.service.RequestFrequencyService;
import com.example.seckill_backend.service.ProductService;
import com.example.seckill_backend.service.ActivityService;
import com.example.seckill_backend.service.SeckillRedisService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderMapper orderMapper;
    private final ActivityProductMapper activityProductMapper;
    private final ProductService productService;
    private final UserMapper userMapper;
    private final CaptchaService captchaService;
    private final RequestFrequencyService requestFrequencyService;
    private final ActivityService activityService;
    private final SeckillRedisService seckillRedisService;
    private final StringRedisTemplate stringRedisTemplate;

    @Value("${seckill.payment.callback.secret:demo-secret}")
    private String paymentCallbackSecret;

    @Value("${seckill.payment.callback.allowed-skew-seconds:300}")
    private long paymentCallbackAllowedSkewSeconds;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order createSeckillOrder(Long userId, Long activityId, Long productId, int quantity, String captchaId, String captchaValue) {

        if (userId == null || activityId == null || productId == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "userId/activityId/productId 不能为空");
        }
        if (quantity <= 0) {
            throw new BizException(ErrorCode.BAD_REQUEST, "quantity 必须大于 0");
        }

        AuthContext.AuthUser current = AuthContext.get();
        if (current != null && current.userId() != null && !isAdmin(current) && !current.userId().equals(userId)) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }

        // 0. 用户认证检查：确保用户存在
        User user = userMapper.getUserById(userId);
        if (user == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "用户不存在");
        }

        // 0.5 频率限制检查：同一用户对同一商品在短时间内只能请求一次
        if (requestFrequencyService.checkFrequency(userId, productId)) {
            throw new BizException(ErrorCode.TOO_FREQUENT);
        }

        // 1. 验证码验证
        if (!captchaService.verifyCaptcha(captchaId, captchaValue, userId, productId)) {
            throw new BizException(ErrorCode.CAPTCHA_INVALID);
        }

        // 记录用户请求，防止重复提交
        requestFrequencyService.recordRequest(userId, productId);

        // 1.5 用户资格校验：根据活动规则检查用户是否有参与资格
        checkUserQualification(user, activityId);

        // 2. 验证活动商品信息
        ActivityProduct activityProduct = activityProductMapper.selectByActivityAndProduct(activityId, productId);
        if (activityProduct == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "活动商品不存在");
        }

        if (!seckillRedisService.tryDeductStock(activityProduct.getId(), activityProduct.getSeckillStock(), quantity)) {
            throw new BizException(ErrorCode.OUT_OF_STOCK);
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == TransactionSynchronization.STATUS_ROLLED_BACK) {
                    seckillRedisService.revertStock(activityProduct.getId(), quantity);
                }
            }
        });

        // 3. 扣减活动商品库存
        if (activityProductMapper.updateSeckillStock(activityProduct.getId(), quantity) <= 0) {
            throw new BizException(ErrorCode.OUT_OF_STOCK);
        }

        // 4. 生成订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setProductId(productId);
        order.setActivityId(activityId);
        order.setSeckillPrice(activityProduct.getSeckillPrice());
        order.setQuantity(quantity);
        order.setTotalAmount(activityProduct.getSeckillPrice().multiply(new BigDecimal(quantity)));
        order.setStatus(0); // 待支付状态

        orderMapper.insert(order);
        return order;
    }

    @Override
    public Order getOrderById(Long id) {
        Order order = orderMapper.selectById(id);
        assertOrderAccessible(order);
        return order;
    }

    @Override
    public Order getOrderByOrderNo(String orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        assertOrderAccessible(order);
        return order;
    }

    @Override
    public List<Order> getOrdersByUserId(Long userId) {
        AuthContext.AuthUser current = AuthContext.get();
        if (current != null && current.userId() != null && !isAdmin(current) && !current.userId().equals(userId)) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        return orderMapper.selectByUserId(userId);
    }

    @Override
    public List<Order> getOrdersByStatus(int status) {
        return orderMapper.selectByStatus(status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        assertOrderAccessible(order);
        if (order == null || order.getStatus() != 0) {
            return false;
        }

        // 更新订单状态为已取消
        order.setStatus(4);
        orderMapper.update(order);

        // 恢复库存
        ActivityProduct activityProduct = activityProductMapper.selectByActivityAndProduct(order.getActivityId(), order.getProductId());
        if (activityProduct != null) {
            activityProductMapper.revertSeckillStock(activityProduct.getId(), order.getQuantity());
            seckillRedisService.revertStock(activityProduct.getId(), order.getQuantity());
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processExpiredOrders() {
        List<Order> expiredOrders = orderMapper.selectExpiredOrders();
        for (Order order : expiredOrders) {
            order.setStatus(5);
            orderMapper.update(order);

            ActivityProduct activityProduct = activityProductMapper.selectByActivityAndProduct(order.getActivityId(), order.getProductId());
            if (activityProduct != null) {
                activityProductMapper.revertSeckillStock(activityProduct.getId(), order.getQuantity());
                seckillRedisService.revertStock(activityProduct.getId(), order.getQuantity());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean payOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        assertOrderAccessible(order);

        if (order == null || order.getStatus() != 0) {
            return false;
        }

        // 更新订单状态为已支付
        order.setStatus(1);
        order.setPaymentTime(new Date());
        orderMapper.update(order);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handlePaymentCallback(String orderNo, BigDecimal amount, long timestamp, String nonce, String signature) {
        assertOrderAccessibleForCallback();
        if (orderNo == null || amount == null || nonce == null || signature == null) {

            throw new BizException(ErrorCode.BAD_REQUEST, "回调参数不能为空");
        }

        long nowSeconds = System.currentTimeMillis() / 1000;
        if (Math.abs(nowSeconds - timestamp) > paymentCallbackAllowedSkewSeconds) {

            throw new BizException(ErrorCode.PAYMENT_REPLAY, "回调时间戳无效");
        }

        String nonceKey = "seckill:paycb:nonce:" + nonce;
        Boolean nonceOk = stringRedisTemplate.opsForValue().setIfAbsent(nonceKey, "1", 10, TimeUnit.MINUTES);
        if (!Boolean.TRUE.equals(nonceOk)) {
            throw new BizException(ErrorCode.PAYMENT_REPLAY);
        }

        String payload = orderNo + "|" + amount.toPlainString() + "|" + timestamp + "|" + nonce;
        String expectedSignature = hmacSha256Hex(paymentCallbackSecret, payload);
        if (!expectedSignature.equalsIgnoreCase(signature)) {
            throw new BizException(ErrorCode.PAYMENT_SIGNATURE_INVALID);
        }

        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "订单不存在");
        }

        if (order.getTotalAmount() == null || order.getTotalAmount().compareTo(amount) != 0) {
            throw new BizException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        if (order.getStatus() != null && order.getStatus() == 1) {
            return true;
        }

        if (order.getStatus() == null || order.getStatus() != 0) {
            return false;
        }

        order.setStatus(1);
        order.setPaymentTime(new Date());
        orderMapper.update(order);
        return true;
    }

    private String hmacSha256Hex(String secret, String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] bytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new BizException(ErrorCode.INTERNAL_ERROR);
        }
    }

    private void assertOrderAccessible(Order order) {
        if (order == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        AuthContext.AuthUser current = AuthContext.get();
        if (current != null && current.userId() != null && !isAdmin(current) && !current.userId().equals(order.getUserId())) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
    }

    private void assertOrderAccessibleForCallback() {
        AuthContext.AuthUser current = AuthContext.get();
        if (current == null || !isAdmin(current)) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
    }

    private boolean isAdmin(AuthContext.AuthUser user) {
        return user.role() != null && "ADMIN".equalsIgnoreCase(user.role());
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 20);
    }

    /**
     * 检查用户是否有参与活动的资格
     * @param user 用户信息
     * @param activityId 活动ID
     */
    private void checkUserQualification(User user, Long activityId) {
        // 获取活动信息
        Activity activity = activityService.getActivityById(activityId);
        if (activity == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "活动不存在");
        }

        // 获取活动规则
        String rule = activity.getRule();
        if (rule == null || rule.isEmpty()) {
            // 如果没有规则，默认允许所有用户参与
            return;
        }

        // 解析活动规则，这里简单实现新用户检查规则
        // 实际项目中可能需要更复杂的规则解析和验证
        if (rule.contains("new_user_only")) {
            // 检查是否为新用户（注册时间在7天内）
            Date registerTime = user.getRegisterTime();
            if (registerTime == null) {
                throw new BizException(ErrorCode.BAD_REQUEST, "用户注册时间未知");
            }

            long sevenDaysAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000;
            if (registerTime.getTime() < sevenDaysAgo) {
                throw new BizException(ErrorCode.FORBIDDEN, "该活动仅对新用户开放");
            }

            //log.info("用户符合新用户条件：userId={}, registerTime={}", user.getId(), registerTime);
        }

        // 可以在这里添加更多的规则检查
        // 例如：用户等级限制、地区限制等
    }
}