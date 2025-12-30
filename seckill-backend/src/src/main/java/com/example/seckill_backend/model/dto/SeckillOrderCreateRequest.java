package com.example.seckill_backend.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SeckillOrderCreateRequest {
    @NotNull
    private Long userId;

    @NotNull
    private Long activityId;

    @NotNull
    private Long productId;

    @Min(1)
    private Integer quantity;

    @NotBlank
    private String captchaId;

    @NotBlank
    private String captchaValue;
}
