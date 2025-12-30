package com.example.seckill_backend.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ActivityStats {
    private Long id;
    private Long activityId;
    private String activityName;
    private Integer pv; // 页面浏览量
    private Integer uv; // 独立访客数
    private Integer participants; // 参与人数
    private Integer soldQuantity; // 售出商品数
    private BigDecimal totalGmv; // 成交总额
    private Integer pendingOrders; // 待支付订单数
    private Integer completedOrders; // 已完成订单数
    private Integer cancelledOrders; // 已取消订单数
}
