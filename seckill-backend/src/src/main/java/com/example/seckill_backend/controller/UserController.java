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

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    /**
     * 用户注册接口
     */
    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody UserRegisterRequest request) {
        userService.register(request.getUsername(), request.getPassword());
        return ApiResponse.success(null);
    }

    /**
     * 用户登录接口
     */
    @PostMapping("/login")
    public ApiResponse<User> login(@Valid @RequestBody UserLoginRequest request, HttpSession session) {
        User loggedIn = userService.login(request.getUsername(), request.getPassword());
        if (loggedIn != null) {
            session.setAttribute("USER_ID", loggedIn.getId());
            session.setAttribute("ROLE", "USER");
            loggedIn.setPassword(null);
        }
        return ApiResponse.success(loggedIn);
    }

    @GetMapping("/me")
    public ApiResponse<User> me() {
        Long userId = AuthContext.requireUserId();
        User profile = userService.getUserById(userId);
        if (profile != null) {
            profile.setPassword(null);
        }
        return ApiResponse.success(profile);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ApiResponse.success(null);
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/profile/{userId}")
    public ApiResponse<User> getUserProfile(@PathVariable Long userId) {
        User profile = userService.getUserById(userId);
        if (profile != null) {
            profile.setPassword(null);
        }
        return ApiResponse.success(profile);
    }

    /**
     * 校验用户参与资格
     */
    @GetMapping("/check-eligibility/{userId}")
    public ApiResponse<Boolean> checkEligibility(@PathVariable Long userId, @RequestParam(required = false) String rule) {
        return ApiResponse.success(userService.checkUserEligibility(userId, rule));
    }

    /**
     * 检查是否为新用户
     */
    @GetMapping("/check-new-user/{userId}")
    public ApiResponse<Boolean> checkNewUser(@PathVariable Long userId, @RequestParam(defaultValue = "7") int days) {
        return ApiResponse.success(userService.isNewUser(userId, days));
    }
}
