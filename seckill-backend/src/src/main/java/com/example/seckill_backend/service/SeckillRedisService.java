package com.example.seckill_backend.service;

public interface SeckillRedisService {
    boolean tryDeductStock(Long activityProductId, Integer initialStock, int quantity);

    void revertStock(Long activityProductId, int quantity);
}
