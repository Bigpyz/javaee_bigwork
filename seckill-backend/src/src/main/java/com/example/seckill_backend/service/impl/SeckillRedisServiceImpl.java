package com.example.seckill_backend.service.impl;

import com.example.seckill_backend.service.SeckillRedisService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;

@Service
public class SeckillRedisServiceImpl implements SeckillRedisService {

    private static final String STOCK_KEY_PREFIX = "seckill:stock:activity_product:";

    private static final DefaultRedisScript<Long> DEDUCT_STOCK_SCRIPT;

    static {
        DEDUCT_STOCK_SCRIPT = new DefaultRedisScript<>();
        DEDUCT_STOCK_SCRIPT.setResultType(Long.class);
        DEDUCT_STOCK_SCRIPT.setScriptText(
                "local stock = tonumber(redis.call('GET', KEYS[1]) or '0');" +
                "local dec = tonumber(ARGV[1]);" +
                "if stock >= dec then " +
                "  redis.call('DECRBY', KEYS[1], dec);" +
                "  return 1;" +
                "else " +
                "  return 0;" +
                "end"
        );
    }

    private final StringRedisTemplate stringRedisTemplate;

    public SeckillRedisServiceImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean tryDeductStock(Long activityProductId, Integer initialStock, int quantity) {
        if (activityProductId == null) {
            return false;
        }
        if (quantity <= 0) {
            return false;
        }

        String key = stockKey(activityProductId);

        if (initialStock != null) {
            stringRedisTemplate.opsForValue().setIfAbsent(key, String.valueOf(initialStock), Duration.ofHours(12));
        }

        Long result = stringRedisTemplate.execute(DEDUCT_STOCK_SCRIPT, Collections.singletonList(key), String.valueOf(quantity));
        return result != null && result == 1L;
    }

    @Override
    public void revertStock(Long activityProductId, int quantity) {
        if (activityProductId == null) {
            return;
        }
        if (quantity <= 0) {
            return;
        }

        String key = stockKey(activityProductId);
        stringRedisTemplate.opsForValue().setIfAbsent(key, "0", Duration.ofHours(12));
        stringRedisTemplate.opsForValue().increment(key, quantity);
    }

    private String stockKey(Long activityProductId) {
        return STOCK_KEY_PREFIX + activityProductId;
    }
}
