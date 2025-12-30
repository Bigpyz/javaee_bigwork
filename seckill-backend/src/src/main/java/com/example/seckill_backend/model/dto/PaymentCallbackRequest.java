package com.example.seckill_backend.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentCallbackRequest {
    @NotBlank
    private String orderNo;

    @NotNull
    private BigDecimal amount;

    @NotNull
    private Long timestamp;

    @NotBlank
    private String nonce;

    @NotBlank
    private String signature;
}
