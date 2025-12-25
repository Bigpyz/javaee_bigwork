package com.example.seckill_backend.service.impl;

import com.example.seckill_backend.service.RequestFrequencyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 请求频率限制服务实现类
 */
@Service
@Slf4j
public class RequestFrequencyServiceImpl implements RequestFrequencyService {

    private final StringRedisTemplate stringRedisTemplate;

    // 时间窗口（毫秒），同一用户对同一商品在该时间内只能请求一次
    private static final long TIME_WINDOW_MS = 1000 * 30; // 30秒

    public RequestFrequencyServiceImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }
    
    @Override
    public boolean checkFrequency(Long userId, Long productId) {
        String key = generateKey(userId, productId);

        Boolean exists = stringRedisTemplate.hasKey(key);
        boolean tooFrequent = Boolean.TRUE.equals(exists);
        if (tooFrequent) {
            log.warn("用户请求频率超过限制：userId={}, productId={}", userId, productId);
        }
        return tooFrequent;
    }
    
    @Override
    public void recordRequest(Long userId, Long productId) {
        String key = generateKey(userId, productId);
        Boolean ok = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", Duration.ofMillis(TIME_WINDOW_MS));
        if (!Boolean.TRUE.equals(ok)) {
            throw new RuntimeException("请求过于频繁，请稍后重试");
        }
        log.info("记录用户请求：userId={}, productId={}", userId, productId);
    }
    
    @Override
    public void clearRequest(Long userId, Long productId) {
        String key = generateKey(userId, productId);
        stringRedisTemplate.delete(key);
        log.info("清除用户请求记录：userId={}, productId={}", userId, productId);
    }
    
    /**
     * 生成请求记录的key
     */
    private String generateKey(Long userId, Long productId) {
        return "seckill:reqfreq:" + userId + ":" + productId;
    }
}