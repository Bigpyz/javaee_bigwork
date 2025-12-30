package com.example.seckill_backend.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品更新请求DTO
 * 用于接收前端请求参数
 */
@Data
public class ProductUpdateRequest {
    @NotBlank
    private String name;

    private String description;

    private String imageUrl;

    private Long activityId;

    @NotNull
    private BigDecimal originalPrice;

    @NotNull
    private BigDecimal seckillPrice;

    @NotNull
    @Min(0)
    private Integer totalStock;

    @NotNull
    @Min(0)
    private Integer seckillStock;

    @NotNull
    private Integer status;
}
