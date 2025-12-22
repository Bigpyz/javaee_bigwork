package com.example.seckill_backend.service.impl;

import com.example.seckill_backend.service.RequestFrequencyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 请求频率限制服务实现类
 */
@Service
@Slf4j
public class RequestFrequencyServiceImpl implements RequestFrequencyService {
    
    // 存储用户对商品的请求记录，key为"userId:productId"，value为请求时间戳
    private final Map<String, Long> requestStore = new ConcurrentHashMap<>();
    
    // 时间窗口（毫秒），同一用户对同一商品在该时间内只能请求一次
    private static final long TIME_WINDOW_MS = 1000 * 30; // 30秒
    
    @Override
    public boolean checkFrequency(Long userId, Long productId) {
        String key = generateKey(userId, productId);
        Long lastRequestTime = requestStore.get(key);
        
        // 如果没有请求记录或请求记录已过期，则返回false表示可以请求
        if (lastRequestTime == null || System.currentTimeMillis() - lastRequestTime > TIME_WINDOW_MS) {
            return false;
        }
        
        // 如果在时间窗口内已经请求过，则返回true表示频率超过限制
        log.warn("用户请求频率超过限制：userId={}, productId={}", userId, productId);
        return true;
    }
    
    @Override
    public void recordRequest(Long userId, Long productId) {
        String key = generateKey(userId, productId);
        requestStore.put(key, System.currentTimeMillis());
        log.info("记录用户请求：userId={}, productId={}", userId, productId);
    }
    
    @Override
    public void clearRequest(Long userId, Long productId) {
        String key = generateKey(userId, productId);
        requestStore.remove(key);
        log.info("清除用户请求记录：userId={}, productId={}", userId, productId);
    }
    
    /**
     * 生成请求记录的key
     */
    private String generateKey(Long userId, Long productId) {
        return userId + ":" + productId;
    }
}