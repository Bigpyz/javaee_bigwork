package com.example.seckill_backend.controller;

import com.example.seckill_backend.common.ApiResponse;
import com.example.seckill_backend.model.dto.CaptchaVerifyRequest;
import com.example.seckill_backend.service.CaptchaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/captcha")
@RequiredArgsConstructor
public class CaptchaController {
    
    private final CaptchaService captchaService;

    @GetMapping("/generate")
    public ApiResponse<Map<String, String>> generateCaptcha(@RequestParam Long userId, @RequestParam Long productId) {
        return ApiResponse.success(captchaService.generateCaptcha(userId, productId));
    }

    @PostMapping("/verify")
    public ApiResponse<Map<String, Boolean>> verifyCaptcha(@Valid @RequestBody CaptchaVerifyRequest request) {
        boolean isValid = captchaService.verifyCaptcha(
                request.getCaptchaId(),
                request.getCaptchaValue(),
                request.getUserId(),
                request.getProductId());
        Map<String, Boolean> result = new HashMap<>();
        result.put("isValid", isValid);
        return ApiResponse.success(result);
    }
}