package com.example.seckill_backend.service;

import java.util.Map;

/**
 * 验证码服务，用于生成和验证验证码，防止机器人刷单
 */
public interface CaptchaService {

    /**
     * 生成验证码
     * @param userId 用户ID
     * @param productId 商品ID
     * @return 包含验证码ID和验证码值的Map
     */
    Map<String, String> generateCaptcha(Long userId, Long productId);
    
    /**
     * 验证验证码
     * @param captchaId 验证码ID
     * @param captchaValue 用户输入的验证码值
     * @param userId 用户ID
     * @param productId 商品ID
     * @return 是否验证通过
     */
    boolean verifyCaptcha(String captchaId, String captchaValue, Long userId, Long productId);
    
    /**
     * 删除验证码
     * @param captchaId 验证码ID
     */
    void removeCaptcha(String captchaId);
}