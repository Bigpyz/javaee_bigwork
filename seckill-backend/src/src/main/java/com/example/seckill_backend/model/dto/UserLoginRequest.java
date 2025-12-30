package com.example.seckill_backend.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserLoginRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
