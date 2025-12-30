package com.example.seckill_backend.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 *商品类
 */
@Data
public class Product {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private Long activityId;
    private BigDecimal originalPrice;
    private BigDecimal seckillPrice;
    private Integer totalStock;
    private Integer seckillStock;
    private Integer status;
    private Date createTime;
    private Date updateTime;
}
