package com.example.seckill_backend.task;

import com.example.seckill_backend.mapper.ActivityProductMapper;
import com.example.seckill_backend.mapper.OrderMapper;
import com.example.seckill_backend.model.ActivityProduct;
import com.example.seckill_backend.model.Order;
import com.example.seckill_backend.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ActivityScheduler {
    private static final Logger logger = LoggerFactory.getLogger(ActivityScheduler.class);
    
    private final ActivityService activityService;
    private final OrderMapper orderMapper;
    private final ActivityProductMapper activityProductMapper;
    
    /**
     * 每分钟执行一次，更新活动状态
     */
    @Scheduled(cron = "0 * * * * ?")
    public void updateActivityStatus() {
        logger.info("定时任务执行：更新活动状态 - {}", new Date());
        activityService.updateActivityStatus();
    }
    
    /**
     * 每分钟执行一次，处理未支付订单的库存回滚
     */
    @Scheduled(cron = "0 * * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void rollbackUnpaidOrders() {
        logger.info("定时任务执行：处理未支付订单库存回滚 - {}", new Date());
        
        // 获取超过30分钟未支付的订单
        List<Order> unpaidOrders = orderMapper.selectUnpaidOrders(30);
        
        for (Order order : unpaidOrders) {
            try {
                // 回滚库存
                ActivityProduct activityProduct = activityProductMapper.selectByActivityAndProduct(order.getActivityId(), order.getProductId());
                if (activityProduct != null) {
                    activityProductMapper.revertSeckillStock(activityProduct.getId(), order.getQuantity());
                }
                
                // 更新订单状态为已取消
                order.setStatus(3); // 3: 已取消
                order.setUpdateTime(new Date());
                orderMapper.update(order);
                
                logger.info("订单 {} 已取消并回滚库存", order.getId());
            } catch (Exception e) {
                logger.error("处理订单 {} 库存回滚失败: {}", order.getId(), e.getMessage());
                // 单个订单处理失败不影响其他订单
            }
        }
    }
}
