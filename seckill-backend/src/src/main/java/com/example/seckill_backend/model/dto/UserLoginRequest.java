package com.example.seckill_backend.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户登录请求DTO
 */
@Data
public class UserLoginRequest {
    @NotBlank  // 验证注解：不能为空
    private String username;  // 用户名

    @NotBlank  // 验证注解：不能为空
    private String password;  // 密码
}
