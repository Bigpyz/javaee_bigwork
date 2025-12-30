package com.example.seckill_backend.mapper;

import com.example.seckill_backend.model.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderMapper {
    int insert(Order order);
    int update(Order order);
    int deleteById(Long id);
    Order selectById(Long id);
    Order selectByOrderNo(String orderNo);
    List<Order> selectByUserId(Long userId);
    List<Order> selectByStatus(int status);
    List<Order> selectByActivityId(Long activityId);
    List<Order> selectExpiredOrders();
    List<Order> selectUnpaidOrders(@Param("minutes") int minutes);
    int updateStatus(@Param("id") Long id, @Param("status") int status);
    int updateStatusIfMatch(@Param("id") Long id, @Param("fromStatus") int fromStatus, @Param("toStatus") int toStatus);
    int markPaid(@Param("id") Long id);
}