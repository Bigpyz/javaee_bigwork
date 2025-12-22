package com.example.seckill_backend.service;

import com.example.seckill_backend.model.User;

public interface UserService {
    /**
     * 用户注册
     */
    void register(String username, String password);

    /**
     * 用户登录
     */
    User login(String username, String password);

    /**
     * 根据ID获取用户信息
     */
    User getUserById(Long id);

    /**
     * 根据用户名获取用户信息
     */
    User getUserByUsername(String username);

    /**
     * 校验用户是否具有参与秒杀活动的资格
     */
    boolean checkUserEligibility(Long userId, String rule);

    /**
     * 检查用户是否为新用户
     */
    boolean isNewUser(Long userId, int days);

    /**
     * 记录用户登录时间
     */
    void recordLoginTime(Long userId);
}