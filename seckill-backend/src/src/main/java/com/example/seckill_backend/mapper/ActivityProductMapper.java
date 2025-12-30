package com.example.seckill_backend.mapper;

import com.example.seckill_backend.model.ActivityProduct;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ActivityProductMapper {
    int insert(ActivityProduct activityProduct);
    int update(ActivityProduct activityProduct);
    int deleteById(Long id);
    ActivityProduct selectById(Long id);
    List<ActivityProduct> selectByActivityId(Long activityId);
    List<ActivityProduct> selectByProductId(Long productId);
    ActivityProduct selectByActivityAndProduct(@Param("activityId") Long activityId, @Param("productId") Long productId);
    int updateSeckillStock(@Param("id") Long id, @Param("decrement") int decrement);
    int revertSeckillStock(@Param("id") Long id, @Param("increment") int increment);
}