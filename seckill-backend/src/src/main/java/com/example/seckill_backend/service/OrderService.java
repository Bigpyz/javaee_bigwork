package com.example.seckill_backend.service;

import com.example.seckill_backend.model.Order;

import java.util.List;

public interface OrderService {
    Order createSeckillOrder(Long userId, Long activityId, Long productId, int quantity, String captchaId, String captchaValue);
    Order getOrderById(Long id);
    Order getOrderByOrderNo(String orderNo);
    List<Order> getOrdersByUserId(Long userId);
    List<Order> getOrdersByStatus(int status);
    boolean cancelOrder(Long orderId);
    boolean payOrder(Long orderId);
    void processExpiredOrders();
    boolean handlePaymentCallback(String orderNo);
}