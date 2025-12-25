package com.example.seckill_backend.service.impl;

import com.example.seckill_backend.service.CaptchaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class CaptchaServiceImpl implements CaptchaService {

    private static final String CAPTCHA_KEY_PREFIX = "seckill:captcha:";

    private final StringRedisTemplate stringRedisTemplate;
    
    // 验证码有效期（秒）
    private static final int CAPTCHA_EXPIRY_SECONDS = 120;

    public CaptchaServiceImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }
    
    @Override
    public Map<String, String> generateCaptcha(Long userId, Long productId) {
        // 生成4位数字验证码
        String captchaValue = String.format("%04d", (int) (Math.random() * 10000));

        
        // 生成验证码ID
        String captchaId = UUID.randomUUID().toString();

        stringRedisTemplate.opsForValue().set(captchaKey(captchaId), captchaValue, Duration.ofSeconds(CAPTCHA_EXPIRY_SECONDS));
        
        log.info("生成验证码：captchaId={}, captchaValue={}, userId={}, productId={}", captchaId, captchaValue, userId, productId);
        
        // 返回包含验证码ID和值的Map
        Map<String, String> result = new HashMap<>();
        result.put("captchaId", captchaId);
        result.put("captchaValue", captchaValue);
        return result;
    }
    
    @Override
    public boolean verifyCaptcha(String captchaId, String captchaValue) {
        if (captchaId == null || captchaValue == null) {
            return false;
        }

        String key = captchaKey(captchaId);
        String expected = stringRedisTemplate.opsForValue().get(key);
        if (expected == null) {
            log.warn("验证码不存在：captchaId={}", captchaId);
            return false;
        }

        // 验证验证码是否正确
        boolean isValid = expected.equals(captchaValue);
        if (isValid) {
            // 验证码验证成功后，删除验证码，防止重复使用
            stringRedisTemplate.delete(key);
            log.info("验证码验证成功：captchaId={}", captchaId);
        } else {
            log.warn("验证码验证失败：captchaId={}, expected={}, actual={}", captchaId, expected, captchaValue);
        }
        
        return isValid;
    }
    
    @Override
    public void removeCaptcha(String captchaId) {
        stringRedisTemplate.delete(captchaKey(captchaId));
        log.info("删除验证码：captchaId={}", captchaId);
    }

    private String captchaKey(String captchaId) {
        return CAPTCHA_KEY_PREFIX + captchaId;
    }
}