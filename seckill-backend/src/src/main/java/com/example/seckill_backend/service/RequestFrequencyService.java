package com.example.seckill_backend.service;

/**
 * 请求频率限制服务
 */
public interface RequestFrequencyService {
    
    /**
     * 检查用户对特定商品的请求频率是否超过限制
     * @param userId 用户ID
     * @param productId 商品ID
     * @return 如果频率超过限制返回true，否则返回false
     */
    boolean checkFrequency(Long userId, Long productId);
    
    /**
     * 记录用户对特定商品的请求
     * @param userId 用户ID
     * @param productId 商品ID
     */
    void recordRequest(Long userId, Long productId);
    
    /**
     * 清除用户对特定商品的请求记录
     * @param userId 用户ID
     * @param productId 商品ID
     */
    void clearRequest(Long userId, Long productId);
}