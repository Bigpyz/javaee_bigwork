package com.example.seckill_backend.controller;

import com.example.seckill_backend.model.User;
import com.example.seckill_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    /**
     * 用户注册接口
     */
    @PostMapping("/register")
    public String register(@RequestBody User user) {
        try {
            userService.register(user.getUsername(), user.getPassword());
            return "注册成功";
        } catch (Exception e) {
            return "注册失败：" + e.getMessage();
        }
    }

    /**
     * 用户登录接口
     */
    @PostMapping("/login")
    public User login(@RequestBody User user) {
        try {
            return userService.login(user.getUsername(), user.getPassword());
        } catch (Exception e) {
            throw new RuntimeException("登录失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/profile/{userId}")
    public User getUserProfile(@PathVariable Long userId) {
        return userService.getUserById(userId);
    }

    /**
     * 校验用户参与资格
     */
    @GetMapping("/check-eligibility/{userId}")
    public boolean checkEligibility(@PathVariable Long userId, @RequestParam(required = false) String rule) {
        return userService.checkUserEligibility(userId, rule);
    }

    /**
     * 检查是否为新用户
     */
    @GetMapping("/check-new-user/{userId}")
    public boolean checkNewUser(@PathVariable Long userId, @RequestParam(defaultValue = "7") int days) {
        return userService.isNewUser(userId, days);
    }
}
