package com.example.seckill_backend.service.impl;

import com.example.seckill_backend.common.BizException;
import com.example.seckill_backend.common.ErrorCode;
import com.example.seckill_backend.mapper.UserMapper;
import com.example.seckill_backend.model.User;
import com.example.seckill_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.Date;

/**
 * 用户服务实现类
 * 实现用户相关的业务逻辑
 */
@Service
@RequiredArgsConstructor  // Lombok注解，自动生成构造函数，注入依赖
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;  // 用户数据访问层依赖

    @Override
    public void register(String username, String password) {
        // 检查用户名是否已存在
        User existingUser = userMapper.getUserByUsername(username);
        if (existingUser != null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "用户名已存在");
        }

        // 对密码进行加密（MD5 方式）
        String encryptedPassword = DigestUtils.md5DigestAsHex(password.getBytes());

        // 创建新用户对象
        User user = new User();
        user.setUsername(username);
        user.setPassword(encryptedPassword);

        // 插入用户到数据库
        userMapper.insertUser(user);
    }

    @Override
    public User login(String username, String password) {
        // 根据用户名查询用户
        User user = userMapper.getUserByUsername(username);
        if (user == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "用户不存在");
        }

        // 对输入的密码进行加密后比对
        String encryptedPassword = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!encryptedPassword.equals(user.getPassword())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "密码错误");
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
        // 如果规则为空，默认允许所有用户参与
        if (rule == null || rule.isEmpty()) {
            return true;
        }

        // 解析规则，这里简单实现，实际项目中可能需要更复杂的规则引擎
        if (rule.contains("new_user")) {
            // 检查是否为新用户（默认7天内注册为新用户）
            return isNewUser(userId, 7);
        }

        // 其他规则默认允许
        return true;
    }

    @Override
    public boolean isNewUser(Long userId, int days) {
        // 查询用户是否在指定天数内注册
        int count = userMapper.countNewUsersByRegisterTime(userId, days);
        return count > 0;
    }

    @Override
    public void recordLoginTime(Long userId) {
        // 更新用户最后登录时间
        userMapper.updateLastLoginTime(userId, new Date());
    }
}
