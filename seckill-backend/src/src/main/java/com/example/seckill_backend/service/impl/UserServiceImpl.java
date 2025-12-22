package com.example.seckill_backend.service.impl;

import com.example.seckill_backend.mapper.UserMapper;
import com.example.seckill_backend.model.User;
import com.example.seckill_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;

    @Override
    public void register(String username, String password) {
        // 检查用户名是否已存在
        User existingUser = userMapper.getUserByUsername(username);
        if (existingUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 对密码进行加密（MD5 方式）
        String encryptedPassword = DigestUtils.md5DigestAsHex(password.getBytes());

        // 创建新用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(encryptedPassword);

        userMapper.insertUser(user);
    }

    @Override
    public User login(String username, String password) {
        User user = userMapper.getUserByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 对输入的密码进行加密后比对
        String encryptedPassword = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!encryptedPassword.equals(user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 记录登录时间
        recordLoginTime(user.getId());

        return user;
    }

    @Override
    public User getUserById(Long id) {
        return userMapper.getUserById(id);
    }

    @Override
    public User getUserByUsername(String username) {
        return userMapper.getUserByUsername(username);
    }

    @Override
    public boolean checkUserEligibility(Long userId, String rule) {
        if (rule == null || rule.isEmpty()) {
            return true; // 无规则限制，默认允许
        }

        // 解析规则，这里简单实现，实际项目中可能需要更复杂的规则引擎
        if (rule.contains("new_user")) {
            // 检查是否为新用户（默认7天内注册为新用户）
            return isNewUser(userId, 7);
        }

        return true;
    }

    @Override
    public boolean isNewUser(Long userId, int days) {
        int count = userMapper.countNewUsersByRegisterTime(userId, days);
        return count > 0;
    }

    @Override
    public void recordLoginTime(Long userId) {
        userMapper.updateLastLoginTime(userId, new Date());
    }
}