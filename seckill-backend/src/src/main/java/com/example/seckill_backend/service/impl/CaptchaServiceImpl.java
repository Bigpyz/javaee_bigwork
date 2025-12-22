package com.example.seckill_backend.service.impl;

import com.example.seckill_backend.service.CaptchaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class CaptchaServiceImpl implements CaptchaService {
    
    // 存储验证码信息的Map，key为验证码ID，value为验证码值
    private final Map<String, CaptchaInfo> captchaStore = new ConcurrentHashMap<>();
    
    // 验证码有效期（秒）
    private static final int CAPTCHA_EXPIRY_SECONDS = 120;
    
    @Override
    public Map<String, String> generateCaptcha(Long userId, Long productId) {
        // 生成4位数字验证码
        String captchaValue = String.format("%04d", (int) (Math.random() * 10000));
        
        // 生成验证码ID
        String captchaId = UUID.randomUUID().toString();
        
        // 存储验证码信息
        CaptchaInfo captchaInfo = new CaptchaInfo(captchaValue, System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(CAPTCHA_EXPIRY_SECONDS));
        captchaStore.put(captchaId, captchaInfo);
        
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
        
        CaptchaInfo captchaInfo = captchaStore.get(captchaId);
        if (captchaInfo == null) {
            log.warn("验证码不存在：captchaId={}", captchaId);
            return false;
        }
        
        // 检查验证码是否过期
        if (System.currentTimeMillis() > captchaInfo.getExpiryTime()) {
            log.warn("验证码已过期：captchaId={}", captchaId);
            captchaStore.remove(captchaId);
            return false;
        }
        
        // 验证验证码是否正确
        boolean isValid = captchaInfo.getCaptchaValue().equals(captchaValue);
        if (isValid) {
            // 验证码验证成功后，删除验证码，防止重复使用
            captchaStore.remove(captchaId);
            log.info("验证码验证成功：captchaId={}", captchaId);
        } else {
            log.warn("验证码验证失败：captchaId={}, expected={}, actual={}", captchaId, captchaInfo.getCaptchaValue(), captchaValue);
        }
        
        return isValid;
    }
    
    @Override
    public void removeCaptcha(String captchaId) {
        captchaStore.remove(captchaId);
        log.info("删除验证码：captchaId={}", captchaId);
    }
    
    // 内部类，用于存储验证码信息
    private static class CaptchaInfo {
        private final String captchaValue;
        private final long expiryTime;
        
        public CaptchaInfo(String captchaValue, long expiryTime) {
            this.captchaValue = captchaValue;
            this.expiryTime = expiryTime;
        }
        
        public String getCaptchaValue() {
            return captchaValue;
        }
        
        public long getExpiryTime() {
            return expiryTime;
        }
    }
}