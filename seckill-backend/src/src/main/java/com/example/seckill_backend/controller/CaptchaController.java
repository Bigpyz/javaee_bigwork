package com.example.seckill_backend.controller;

import com.example.seckill_backend.service.CaptchaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/captcha")
@RequiredArgsConstructor
public class CaptchaController {
    
    private final CaptchaService captchaService;
    
    /**
     * 生成验证码
     * @param userId 用户ID
     * @param productId 商品ID
     * @return 验证码ID
     */
    @GetMapping("/generate")
    public Map<String, String> generateCaptcha(@RequestParam Long userId, @RequestParam Long productId) {
        // 直接返回包含验证码ID和值的Map
        return captchaService.generateCaptcha(userId, productId);
    }
    
    /**
     * 验证验证码
     * @param captchaId 验证码ID
     * @param captchaValue 用户输入的验证码值
     * @return 验证结果
     */
    @PostMapping("/verify")
    public Map<String, Boolean> verifyCaptcha(@RequestParam String captchaId, @RequestParam String captchaValue) {
        boolean isValid = captchaService.verifyCaptcha(captchaId, captchaValue);
        Map<String, Boolean> result = new HashMap<>();
        result.put("isValid", isValid);
        return result;
    }
}