package com.example.seckill_backend.service.impl;

import com.example.seckill_backend.mapper.ActivityMapper;
import com.example.seckill_backend.mapper.ActivityProductMapper;
import com.example.seckill_backend.mapper.ActivityStatsMapper;
import com.example.seckill_backend.mapper.ActivityVisitMapper;
import com.example.seckill_backend.mapper.OrderMapper;
import com.example.seckill_backend.model.Activity;
import com.example.seckill_backend.model.ActivityProduct;
import com.example.seckill_backend.model.ActivityStats;
import com.example.seckill_backend.model.Product;
import com.example.seckill_backend.model.dto.ActivityWithProductsDTO;
import com.example.seckill_backend.model.dto.SeckillProductCreateDTO;
import com.example.seckill_backend.service.ActivityService;
import com.example.seckill_backend.service.ActivityStatsService;
import com.example.seckill_backend.service.ProductService;
import com.example.seckill_backend.task.ActivityScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService, ActivityStatsService {
    private final ActivityMapper activityMapper;
    private final ActivityProductMapper activityProductMapper;
    private final ActivityStatsMapper activityStatsMapper;
    private final ActivityVisitMapper activityVisitMapper;
    private final OrderMapper orderMapper;
    private final ProductService productService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Activity createActivity(Activity activity) {
        activity.setStatus(0); // 默认为待开始状态
        activityMapper.insert(activity);
        return activity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Activity createActivityWithProducts(ActivityWithProductsDTO activityWithProductsDTO) {
        // 创建活动
        Activity activity = activityWithProductsDTO.getActivity();
        activity.setStatus(0); // 默认为待开始状态
        activityMapper.insert(activity);
        
        // 批量创建商品和活动商品
        List<SeckillProductCreateDTO> seckillProducts = activityWithProductsDTO.getSeckillProducts();
        if (seckillProducts != null && !seckillProducts.isEmpty()) {
            for (SeckillProductCreateDTO seckillProductDTO : seckillProducts) {
                // 1. 创建新商品
                Product product = new Product();
                product.setName(seckillProductDTO.getName());
                product.setDescription(seckillProductDTO.getDescription());
                product.setImageUrl(seckillProductDTO.getImageUrl());
                product.setActivityId(activity.getId()); // 设置活动ID
                product.setOriginalPrice(seckillProductDTO.getOriginalPrice());
                product.setTotalStock(seckillProductDTO.getTotalStock());
                product.setSeckillPrice(seckillProductDTO.getSeckillPrice());
                product.setSeckillStock(seckillProductDTO.getSeckillStock());
                product.setStatus(1); // 1表示上架状态
                product.setCreateTime(new Date());
                product.setUpdateTime(new Date());
                
                // 保存商品
                Product savedProduct = productService.createProduct(product);
                
                // 2. 创建活动商品关联
                ActivityProduct activityProduct = new ActivityProduct();
                activityProduct.setActivityId(activity.getId());
                activityProduct.setProductId(savedProduct.getId());
                activityProduct.setSeckillPrice(seckillProductDTO.getSeckillPrice());
                activityProduct.setSeckillStock(seckillProductDTO.getSeckillStock());
                activityProduct.setLimitPerUser(seckillProductDTO.getLimitPerUser());
                activityProduct.setCreateTime(new Date());
                activityProduct.setUpdateTime(new Date());
                
                // 保存活动商品
                activityProductMapper.insert(activityProduct);
            }
        }
        
        return activity;
    }

    @Override
    public Activity updateActivity(Activity activity) {
        activityMapper.update(activity);
        return activity;
    }

    @Override
    public void deleteActivity(Long id) {
        activityMapper.deleteById(id);
    }

    @Override
    public Activity getActivityById(Long id) {
        return activityMapper.selectById(id);
    }

    @Override
    public List<Activity> getAllActivities() {
        return activityMapper.selectAll();
    }

    @Override
    public List<Activity> getActivitiesByStatus(int status) {
        return activityMapper.selectByStatus(status);
    }

    @Override
    public List<Activity> getUpcomingActivities() {
        return activityMapper.selectUpcoming();
    }

    @Override
    public List<Activity> getActiveActivities() {
        return activityMapper.selectActive();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean startActivity(Long id) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null || activity.getStatus() != 0) {
            return false;
        }
        return activityMapper.updateStatus(id, 1) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean endActivity(Long id) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            return false;
        }
        boolean updated = activityMapper.updateStatus(id, 2) > 0;
        if (updated) {
            // 同步扣减Product表库存
            syncProductStockForActivity(id);
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ActivityProduct addProductToActivity(ActivityProduct activityProduct) {
        activityProductMapper.insert(activityProduct);
        return activityProduct;
    }

    @Override
    public List<ActivityProduct> getProductsByActivityId(Long activityId) {
        return activityProductMapper.selectByActivityId(activityId);
    }

    private static final ThreadLocal<SimpleDateFormat> DATE_FORMATTER = ThreadLocal.withInitial(
            () -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS") // 精确到毫秒，便于精准对比
    );

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateActivityStatus() {
        Logger logger = LoggerFactory.getLogger(ActivityServiceImpl.class);
        List<Activity> activities = activityMapper.selectAll();
        Date now = new Date();

        // 打印当前时间（格式化后，更易读）
        String nowStr = DATE_FORMATTER.get().format(now);
        logger.info("开始更新活动状态，当前系统时间：{}，待处理活动总数：{}", nowStr, activities.size());

        for (Activity activity : activities) {
            Long activityId = activity.getId();
            String activityName = activity.getName();
            Date startTime = activity.getStartTime();
            Date endTime = activity.getEndTime();
            int currentStatus = activity.getStatus();

            // 格式化活动的时间
            String startTimeStr = startTime == null ? "null" : DATE_FORMATTER.get().format(startTime);
            String endTimeStr = endTime == null ? "null" : DATE_FORMATTER.get().format(endTime);

            // 打印当前活动的基础信息
            logger.info("处理活动【ID：{}，名称：{}】，开始时间：{}，结束时间：{}，当前状态：{}",
                    activityId, activityName, startTimeStr, endTimeStr, currentStatus);

            // 状态判断逻辑
            if (startTime != null && startTime.after(now) && currentStatus != 0) {
                // 活动未开始
                activity.setStatus(0);
                activityMapper.update(activity);
                logger.info("活动【ID：{}，名称：{}】状态更新：{} → 0（未开始），原因：开始时间{}晚于当前时间{}",
                        activityId, activityName, currentStatus, startTimeStr, nowStr);
            } else if (startTime != null && endTime != null
                    && startTime.before(now) && endTime.after(now) && currentStatus != 1) {
                // 活动进行中
                activity.setStatus(1);
                activityMapper.update(activity);
                logger.info("活动【ID：{}，名称：{}】状态更新：{} → 1（进行中），原因：开始时间{}早于当前时间{}，结束时间{}晚于当前时间{}",
                        activityId, activityName, currentStatus, startTimeStr, nowStr, endTimeStr, nowStr);
            } else if (endTime != null && endTime.before(now) && currentStatus != 2) {
                // 活动已结束
                activity.setStatus(2);
                activityMapper.update(activity);
                logger.info("活动【ID：{}，名称：{}】状态更新：{} → 2（已结束），原因：结束时间{}早于当前时间{}",
                        activityId, activityName, currentStatus, endTimeStr, nowStr);
                
                // 同步扣减Product表库存
                syncProductStockForActivity(activityId);
            } else {
                // 无状态更新的情况，打印debug日志
                logger.debug("活动【ID：{}，名称：{}】无需更新状态，当前状态已匹配时间条件", activityId, activityName);
            }
        }

        logger.info("活动状态更新任务执行完成");
        // 移除ThreadLocal中的格式化器，避免内存泄漏
        DATE_FORMATTER.remove();
    }

    /**
     * 活动结束后，根据已售数量同步扣减Product表库存
     */
    private void syncProductStockForActivity(Long activityId) {
        Logger logger = LoggerFactory.getLogger(ActivityServiceImpl.class);
        
        // 获取该活动的订单，统计每个商品的售出数量
        List<com.example.seckill_backend.model.Order> orders = orderMapper.selectByActivityId(activityId);
        java.util.Map<Long, Integer> soldMap = new java.util.HashMap<>();
        for (com.example.seckill_backend.model.Order order : orders) {
            // 只统计已支付(1)、已发货(2)、已完成(3)的订单
            if (order.getStatus() != null && order.getStatus() >= 1 && order.getStatus() <= 3) {
                soldMap.merge(order.getProductId(), order.getQuantity(), Integer::sum);
            }
        }
        
        // 更新Product表库存
        for (java.util.Map.Entry<Long, Integer> entry : soldMap.entrySet()) {
            Long productId = entry.getKey();
            int soldQuantity = entry.getValue();
            
            try {
                Product product = productService.getProductById(productId);
                int newTotalStock = Math.max(0, product.getTotalStock() - soldQuantity);
                int newSeckillStock = Math.max(0, product.getSeckillStock() - soldQuantity);
                product.setTotalStock(newTotalStock);
                product.setSeckillStock(newSeckillStock);
                productService.updateProduct(product);
                logger.info("活动[{}]结束，商品[{}]库存已同步，售出数量: {}", activityId, productId, soldQuantity);
            } catch (Exception e) {
                logger.error("活动[{}]结束，商品[{}]库存同步失败: {}", activityId, productId, e.getMessage());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ActivityStats getActivityStats(Long activityId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            return null;
        }

        // 从数据库获取PV和UV
        int pv = activityVisitMapper.countPVByActivityId(activityId);
        int uv = activityVisitMapper.countUVByActivityId(activityId);

        // 获取活动相关订单
        List<com.example.seckill_backend.model.Order> orders = orderMapper.selectByActivityId(activityId);
        
        // 统计订单数据
        int participants = 0;
        int soldQuantity = 0;
        BigDecimal totalGmv = BigDecimal.ZERO;
        int pendingOrders = 0;
        int completedOrders = 0;
        int cancelledOrders = 0;

        for (com.example.seckill_backend.model.Order order : orders) {
            participants++;
            soldQuantity += order.getQuantity();
            totalGmv = totalGmv.add(order.getTotalAmount());

            switch (order.getStatus()) {
                case 0: // 待支付
                    pendingOrders++;
                    break;
                case 1: // 已支付
                case 2: // 已发货
                case 3: // 已完成
                    completedOrders++;
                    break;
                case 4: // 已取消
                case 5: // 超时未支付
                    cancelledOrders++;
                    break;
            }
        }

        // 创建或更新统计数据
        ActivityStats stats = activityStatsMapper.selectByActivityId(activityId);
        if (stats == null) {
            stats = new ActivityStats();
            stats.setActivityId(activityId);
            stats.setActivityName(activity.getName());
            stats.setPv(pv);
            stats.setUv(uv);
            stats.setParticipants(participants);
            stats.setSoldQuantity(soldQuantity);
            stats.setTotalGmv(totalGmv);
            stats.setPendingOrders(pendingOrders);
            stats.setCompletedOrders(completedOrders);
            stats.setCancelledOrders(cancelledOrders);
            activityStatsMapper.insert(stats);
        } else {
            stats.setActivityName(activity.getName());
            stats.setPv(pv);
            stats.setUv(uv);
            stats.setParticipants(participants);
            stats.setSoldQuantity(soldQuantity);
            stats.setTotalGmv(totalGmv);
            stats.setPendingOrders(pendingOrders);
            stats.setCompletedOrders(completedOrders);
            stats.setCancelledOrders(cancelledOrders);
            activityStatsMapper.update(stats);
        }

        return stats;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordVisit(Long activityId, Long userId, String ipAddress, String userAgent) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            return;
        }
        
        // 检查是否为新访客
        int existingVisits = activityVisitMapper.checkIfUniqueVisitor(activityId, userId, ipAddress);
        int isUnique = existingVisits > 0 ? 0 : 1;
        
        // 记录访问日志
        com.example.seckill_backend.model.ActivityVisit visit = new com.example.seckill_backend.model.ActivityVisit();
        visit.setActivityId(activityId);
        visit.setUserId(userId);
        visit.setIpAddress(ipAddress);
        visit.setUserAgent(userAgent);
        visit.setIsUnique(isUnique);
        activityVisitMapper.insert(visit);
        
        // 更新统计数据
        if (isUnique == 1) {
            activityStatsMapper.updatePVAndUV(activityId, 1, 1);
        } else {
            activityStatsMapper.updatePVAndUV(activityId, 1, 0);
        }
    }
}