package com.example.seckill_backend.service;

import com.example.seckill_backend.model.ActivityStats;

public interface ActivityStatsService {
    /**
     * 获取活动统计数据
     * @param activityId 活动ID
     * @return 活动统计数据
     */
    ActivityStats getActivityStats(Long activityId);
    
    /**
     * 记录访问信息
     * @param activityId 活动ID
     * @param userId 用户ID
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     */
    void recordVisit(Long activityId, Long userId, String ipAddress, String userAgent);
}