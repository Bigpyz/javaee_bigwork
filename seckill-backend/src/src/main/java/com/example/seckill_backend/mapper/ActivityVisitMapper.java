package com.example.seckill_backend.mapper;

import com.example.seckill_backend.model.ActivityVisit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ActivityVisitMapper {
    int insert(ActivityVisit activityVisit);
    int countPVByActivityId(Long activityId);
    int countUVByActivityId(Long activityId);
    int checkIfUniqueVisitor(@Param("activityId") Long activityId, @Param("userId") Long userId, @Param("ipAddress") String ipAddress);
}