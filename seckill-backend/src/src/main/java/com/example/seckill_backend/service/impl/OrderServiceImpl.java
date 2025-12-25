package com.example.seckill_backend.service.impl;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order createSeckillOrder(Long userId, Long activityId, Long productId, int quantity, String captchaId, String captchaValue) {

        // 0. 用户认证检查：确保用户存在
        User user = userMapper.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在，无法创建订单");
        }
        
        // 0.5 频率限制检查：同一用户对同一商品在短时间内只能请求一次
        if (requestFrequencyService.checkFrequency(userId, productId)) {
            throw new RuntimeException("请求过于频繁，请稍后重试");
        }
        
        // 1. 验证码验证
        if (!captchaService.verifyCaptcha(captchaId, captchaValue)) {
            throw new RuntimeException("验证码错误或已过期");
        }
        
        // 记录用户请求，防止重复提交
        requestFrequencyService.recordRequest(userId, productId);
        
        // 1.5 用户资格校验：根据活动规则检查用户是否有参与资格
        checkUserQualification(user, activityId);
        
        // 2. 验证活动商品信息
        ActivityProduct activityProduct = activityProductMapper.selectByActivityAndProduct(activityId, productId);
        if (activityProduct == null) {
            throw new RuntimeException("活动商品不存在");
        }

        if (!seckillRedisService.tryDeductStock(activityProduct.getId(), activityProduct.getSeckillStock(), quantity)) {
            throw new RuntimeException("商品库存不足");
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
            seckillRedisService.revertStock(activityProduct.getId(), quantity);
            throw new RuntimeException("商品库存不足");
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
        return orderMapper.selectById(id);
    }

    @Override
    public Order getOrderByOrderNo(String orderNo) {
        return orderMapper.selectByOrderNo(orderNo);
    }

    @Override
    public List<Order> getOrdersByUserId(Long userId) {
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
    public boolean payOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
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
    public void processExpiredOrders() {
        List<Order> expiredOrders = orderMapper.selectExpiredOrders();
        for (Order order : expiredOrders) {
            // 更新订单状态为超时未支付
            order.setStatus(5);
            orderMapper.update(order);

            // 恢复库存
            ActivityProduct activityProduct = activityProductMapper.selectByActivityAndProduct(order.getActivityId(), order.getProductId());
            if (activityProduct != null) {
                activityProductMapper.revertSeckillStock(activityProduct.getId(), order.getQuantity());
                seckillRedisService.revertStock(activityProduct.getId(), order.getQuantity());
            }
        }
    }

    @Override
    public boolean handlePaymentCallback(String orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null || order.getStatus() != 0) {
            return false;
        }

        // 更新订单状态为已支付
        order.setStatus(1);
        order.setPaymentTime(new Date());
        orderMapper.update(order);

        return true;
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
            throw new RuntimeException("活动不存在");
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
                throw new RuntimeException("用户注册时间未知，无法参与活动");
            }
            
            long sevenDaysAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000;
            if (registerTime.getTime() < sevenDaysAgo) {
                throw new RuntimeException("该活动仅对新用户开放");
            }
            
            //log.info("用户符合新用户条件：userId={}, registerTime={}", user.getId(), registerTime);
        }
        
        // 可以在这里添加更多的规则检查
        // 例如：用户等级限制、地区限制等
    }
}