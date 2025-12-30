package com.example.seckill_backend.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品创建请求DTO
 * 用于接收前端请求参数
 */
@Data
public class ProductCreateRequest {
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

    /**
     * 商品状态
     * 可选字段，1=启用（默认）、0=禁用；若前端不传，后端会设置默认值
     */
    private Integer status;
}
