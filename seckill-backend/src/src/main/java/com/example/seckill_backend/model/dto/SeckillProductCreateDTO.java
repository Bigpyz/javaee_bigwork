package com.example.seckill_backend.model.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class SeckillProductCreateDTO {
    // 商品基本信息
    private String name;
    private String description;
    private String imageUrl;
    private BigDecimal originalPrice;
    private Integer totalStock;
    
    // 秒杀活动商品信息
    private BigDecimal seckillPrice;
    private Integer seckillStock;
    private Integer limitPerUser;
}