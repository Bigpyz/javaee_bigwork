package com.example.seckill_backend.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CaptchaVerifyRequest {
    @NotBlank
    private String captchaId;

    @NotBlank
    private String captchaValue;

    @NotNull
    private Long userId;

    @NotNull
    private Long productId;
}
