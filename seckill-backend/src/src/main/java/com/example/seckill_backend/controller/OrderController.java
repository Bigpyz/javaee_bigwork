package com.example.seckill_backend.controller;

import com.example.seckill_backend.common.ApiResponse;
import com.example.seckill_backend.model.dto.PaymentCallbackRequest;
import com.example.seckill_backend.model.dto.SeckillOrderCreateRequest;
import com.example.seckill_backend.model.Order;
import com.example.seckill_backend.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    /**
     *
     *
     * @param request
     * @return
     */
    @PostMapping("/seckill")
    public ApiResponse<Order> createSeckillOrder(@Valid @RequestBody SeckillOrderCreateRequest request) {
        int quantity = request.getQuantity() == null ? 1 : request.getQuantity();
        return ApiResponse.success(orderService.createSeckillOrder(
                request.getUserId(),
                request.getActivityId(),
                request.getProductId(),
                quantity,
                request.getCaptchaId(),
                request.getCaptchaValue()));
    }

    /**
     * id
     *
     * @param id
     * @return
     */
    @PostMapping("/{id}/cancel")
    public ApiResponse<Boolean> cancelOrder(@PathVariable Long id) {
        return ApiResponse.success(orderService.cancelOrder(id));
    }

    /**
     * id
     *
     * @param id
     * @return
     */
    @PostMapping("/{id}/pay")
    public ApiResponse<Boolean> payOrder(@PathVariable Long id) {
        return ApiResponse.success(orderService.payOrder(id));
    }

    /**
     *
     *
     * @param request
     * @return
     */
    @PostMapping("/payment-callback")
    public ApiResponse<Boolean> handlePaymentCallback(@Valid @RequestBody PaymentCallbackRequest request) {
        return ApiResponse.success(orderService.handlePaymentCallback(
                request.getOrderNo(),
                request.getAmount(),
                request.getTimestamp(),
                request.getNonce(),
                request.getSignature()));
    }

    /**
     * id
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public ApiResponse<Order> getOrderById(@PathVariable Long id) {
        return ApiResponse.success(orderService.getOrderById(id));
    }

    /**
     * orderNo
     *
     * @param orderNo
     * @return
     */
    @GetMapping("/order-no/{orderNo}")
    public ApiResponse<Order> getOrderByOrderNo(@PathVariable String orderNo) {
        return ApiResponse.success(orderService.getOrderByOrderNo(orderNo));
    }

    /**
     * userId
     *
     * @param userId
     * @return
     */
    @GetMapping("/user/{userId}")
    public ApiResponse<List<Order>> getOrdersByUserId(@PathVariable Long userId) {
        return ApiResponse.success(orderService.getOrdersByUserId(userId));
    }

    /**
     * status
     *
     * @param status
     * @return
     */
    @GetMapping("/status/{status}")
    public ApiResponse<List<Order>> getOrdersByStatus(@PathVariable int status) {
        return ApiResponse.success(orderService.getOrdersByStatus(status));
    }



    /**
     *
     *
     * @return
     */
    @PostMapping("/process-expired")
    public ApiResponse<Void> processExpiredOrders() {
        orderService.processExpiredOrders();
        return ApiResponse.success(null);
    }

}