package com.example.seckill_backend.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class RateLimiter {
    private static final ConcurrentHashMap<String, RateLimitInfo> RATE_LIMIT_MAP = new ConcurrentHashMap<>();

    /**
     * 检查用户是否超过请求频率限制
     * @param key 限制的唯一标识（如：userId:activityId）
     * @param limit 时间窗口内允许的最大请求数
     * @param timeWindow 时间窗口（毫秒）
     * @return 是否允许请求
     */
    public static boolean allowRequest(String key, int limit, long timeWindow) {
        long now = System.currentTimeMillis();
        RateLimitInfo info = RATE_LIMIT_MAP.get(key);

        if (info == null) {
            // 首次请求
            info = new RateLimitInfo(now, 1);
            RATE_LIMIT_MAP.put(key, info);
            return true;
        }

        // 检查是否在时间窗口内
        if (now - info.getLastRequestTime() > timeWindow) {
            // 时间窗口已过，重置计数
            info.setLastRequestTime(now);
            info.setCount(1);
            return true;
        }

        // 检查是否超过限制
        if (info.getCount() < limit) {
            info.setCount(info.getCount() + 1);
            return true;
        }

        return false;
    }

    /**
     * 清除指定key的限制记录
     */
    public static void clearLimit(String key) {
        RATE_LIMIT_MAP.remove(key);
    }

    private static class RateLimitInfo {
        private long lastRequestTime;
        private int count;

        public RateLimitInfo(long lastRequestTime, int count) {
            this.lastRequestTime = lastRequestTime;
            this.count = count;
        }

        public long getLastRequestTime() {
            return lastRequestTime;
        }

        public void setLastRequestTime(long lastRequestTime) {
            this.lastRequestTime = lastRequestTime;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }
}