package com.example.seckill_backend.controller;

import com.example.seckill_backend.model.Order;
import com.example.seckill_backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/seckill")
    public Order createSeckillOrder(@RequestParam Long userId,
                                    @RequestParam Long activityId,
                                    @RequestParam Long productId,
                                    @RequestParam(defaultValue = "1") int quantity,
                                    @RequestParam String captchaId,
                                    @RequestParam String captchaValue) {
        // 用户认证检查，确保用户存在
        return orderService.createSeckillOrder(userId, activityId, productId, quantity, captchaId, captchaValue);
    }

    @GetMapping("/{id}")
    public Order getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @GetMapping("/order-no/{orderNo}")
    public Order getOrderByOrderNo(@PathVariable String orderNo) {
        return orderService.getOrderByOrderNo(orderNo);
    }

    @GetMapping("/user/{userId}")
    public List<Order> getOrdersByUserId(@PathVariable Long userId) {
        return orderService.getOrdersByUserId(userId);
    }

    @GetMapping("/status/{status}")
    public List<Order> getOrdersByStatus(@PathVariable int status) {
        return orderService.getOrdersByStatus(status);
    }

    @PostMapping("/{id}/cancel")
    public boolean cancelOrder(@PathVariable Long id) {
        return orderService.cancelOrder(id);
    }

    @PostMapping("/{id}/pay")
    public boolean payOrder(@PathVariable Long id) {
        return orderService.payOrder(id);
    }

    @PostMapping("/process-expired")
    public void processExpiredOrders() {
        orderService.processExpiredOrders();
    }

    @PostMapping("/payment-callback")
    public boolean handlePaymentCallback(@RequestParam String orderNo) {
        return orderService.handlePaymentCallback(orderNo);
    }
}