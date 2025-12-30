package com.example.seckill_backend.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class Order {
    private Long id;
    private String orderNo;
    private Long userId;
    private Long productId;
    private Long activityId;
    private BigDecimal seckillPrice;
    private Integer quantity;
    private BigDecimal totalAmount;
    private Integer status;
    private Date createTime;
    private Date updateTime;
    private Date paymentTime;
}