package com.example.seckill_backend.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class ActivityProduct {
    private Long id;
    private Long activityId;
    private Long productId;
    private BigDecimal seckillPrice;
    private Integer seckillStock;
    private Integer limitPerUser;
    private Date createTime;
    private Date updateTime;
}