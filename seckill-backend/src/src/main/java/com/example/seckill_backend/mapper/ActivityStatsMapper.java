package com.example.seckill_backend.mapper;

import com.example.seckill_backend.model.ActivityStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ActivityStatsMapper {
    ActivityStats selectByActivityId(Long activityId);
    int insert(ActivityStats activityStats);
    int update(ActivityStats activityStats);
    int updatePVAndUV(@Param("activityId") Long activityId, @Param("pv") int pv, @Param("uv") int uv);
    int updateOrderStats(@Param("activityId") Long activityId, @Param("participants") int participants,
                         @Param("soldQuantity") int soldQuantity, @Param("totalGmv") java.math.BigDecimal totalGmv,
                         @Param("pendingOrders") int pendingOrders, @Param("completedOrders") int completedOrders,
                         @Param("cancelledOrders") int cancelledOrders);
}