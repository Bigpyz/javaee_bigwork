package com.example.seckill_backend.service.impl;

import com.example.seckill_backend.common.BizException;
import com.example.seckill_backend.common.ErrorCode;
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
    private static final String CAPTCHA_RATE_KEY_PREFIX = "seckill:captcha:rate:";

    private final StringRedisTemplate stringRedisTemplate;
    
    // 验证码有效期（秒）
    private static final int CAPTCHA_EXPIRY_SECONDS = 120;
    private static final int CAPTCHA_RATE_LIMIT_SECONDS = 2;

    public CaptchaServiceImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }
    
    @Override
    public Map<String, String> generateCaptcha(Long userId, Long productId) {
        if (userId == null || productId == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "userId/productId 不能为空");
        }

        String rateKey = rateKey(userId, productId);
        Boolean ok = stringRedisTemplate.opsForValue().setIfAbsent(rateKey, "1", Duration.ofSeconds(CAPTCHA_RATE_LIMIT_SECONDS));
        if (!Boolean.TRUE.equals(ok)) {
            throw new BizException(ErrorCode.CAPTCHA_RATE_LIMITED);
        }

        int a = 1 + (int) (Math.random() * 9);
        int b = 1 + (int) (Math.random() * 9);
        String question = a + " + " + b + " = ?";
        String answer = String.valueOf(a + b);

        String captchaId = UUID.randomUUID().toString();

        String value = userId + ":" + productId + ":" + answer;
        stringRedisTemplate.opsForValue().set(captchaKey(captchaId), value, Duration.ofSeconds(CAPTCHA_EXPIRY_SECONDS));

        log.info("生成验证码：captchaId={}, question={}, userId={}, productId={}", captchaId, question, userId, productId);

        Map<String, String> result = new HashMap<>();
        result.put("captchaId", captchaId);
        result.put("question", question);
        return result;
    }
    
    @Override
    public boolean verifyCaptcha(String captchaId, String captchaValue, Long userId, Long productId) {
        if (captchaId == null || captchaValue == null || userId == null || productId == null) {
            return false;
        }

        String key = captchaKey(captchaId);
        String stored = stringRedisTemplate.opsForValue().get(key);
        if (stored == null) {
            log.warn("验证码不存在：captchaId={}", captchaId);
            return false;
        }

        String prefix = userId + ":" + productId + ":";
        if (!stored.startsWith(prefix)) {
            log.warn("验证码绑定信息不匹配：captchaId={}, userId={}, productId={}", captchaId, userId, productId);
            return false;
        }

        String expectedAnswer = stored.substring(prefix.length());

        boolean isValid = expectedAnswer.equals(captchaValue);
        if (isValid) {
            stringRedisTemplate.delete(key);
            log.info("验证码验证成功：captchaId={}", captchaId);
        } else {
            log.warn("验证码验证失败：captchaId={}, actual={}", captchaId, captchaValue);
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

    private String rateKey(Long userId, Long productId) {
        return CAPTCHA_RATE_KEY_PREFIX + userId + ":" + productId;
    }
}