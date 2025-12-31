package com.example.seckill_backend.controller;

import com.example.seckill_backend.common.ApiResponse;
import com.example.seckill_backend.common.AuthContext;
import com.example.seckill_backend.model.dto.UserLoginRequest;
import com.example.seckill_backend.model.dto.UserRegisterRequest;
import com.example.seckill_backend.model.User;
import com.example.seckill_backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 * 负责处理用户相关的HTTP请求，包括注册、登录、获取用户信息等
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor  // Lombok注解，自动生成构造函数，注入依赖
public class UserController {
    private final UserService userService;  // 用户服务层依赖

    /**
     * 用户注册接口
     * @param request 包含用户名和密码的注册请求对象
     * @return 无数据的成功响应
     */
    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody UserRegisterRequest request) {
        userService.register(request.getUsername(), request.getPassword());
        return ApiResponse.success(null);
    }

    /**
     * 用户登录接口
     * @param request 包含用户名和密码的登录请求对象
     * @param session HTTP会话对象，用于存储用户登录状态
     * @return 包含用户信息的成功响应
     */
    @PostMapping("/login")
    public ApiResponse<User> login(@Valid @RequestBody UserLoginRequest request, HttpSession session) {
        User loggedIn = userService.login(request.getUsername(), request.getPassword());
        if (loggedIn != null) {
            // 将用户ID和角色存入Session，实现会话管理
            session.setAttribute("USER_ID", loggedIn.getId());
            session.setAttribute("ROLE", "USER");
            // 清除密码字段，防止敏感信息泄露
            loggedIn.setPassword(null);
        }
        return ApiResponse.success(loggedIn);
    }

    /**
     * 获取当前登录用户信息接口
     * @return 包含当前用户信息的成功响应
     */
    @GetMapping("/me")
    public ApiResponse<User> me() {
        // 从AuthContext获取当前登录用户的ID
        Long userId = AuthContext.requireUserId();
        User profile = userService.getUserById(userId);
        if (profile != null) {
            // 清除密码字段，防止敏感信息泄露
            profile.setPassword(null);
        }
        return ApiResponse.success(profile);
    }

    /**
     * 用户登出接口
     * @param request HTTP请求对象，用于获取当前会话
     * @return 无数据的成功响应
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        // 获取当前会话并使其失效，清除登录状态
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ApiResponse.success(null);
    }

    /**
     * 获取指定用户信息接口
     * @param userId 用户ID
     * @return 包含用户信息的成功响应
     */
    @GetMapping("/profile/{userId}")
    public ApiResponse<User> getUserProfile(@PathVariable Long userId) {
        User profile = userService.getUserById(userId);
        if (profile != null) {
            // 清除密码字段，防止敏感信息泄露
            profile.setPassword(null);
        }
        return ApiResponse.success(profile);
    }

    /**
     * 校验用户参与资格接口
     * @param userId 用户ID
     * @param rule 资格规则，如"new_user"表示新用户规则
     * @return 包含资格检查结果的响应
     */
    @GetMapping("/check-eligibility/{userId}")
    public ApiResponse<Boolean> checkEligibility(@PathVariable Long userId, @RequestParam(required = false) String rule) {
        return ApiResponse.success(userService.checkUserEligibility(userId, rule));
    }

    /**
     * 检查是否为新用户接口
     * @param userId 用户ID
     * @param days 判断新用户的天数阈值，默认为7天
     * @return 包含检查结果的响应
     */
    @GetMapping("/check-new-user/{userId}")
    public ApiResponse<Boolean> checkNewUser(@PathVariable Long userId, @RequestParam(defaultValue = "7") int days) {
        return ApiResponse.success(userService.isNewUser(userId, days));
    }
}
