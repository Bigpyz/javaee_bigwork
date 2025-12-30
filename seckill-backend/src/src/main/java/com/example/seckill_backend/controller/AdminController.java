package com.example.seckill_backend.controller;

import com.example.seckill_backend.common.ApiResponse;
import com.example.seckill_backend.model.dto.UserLoginRequest;
import com.example.seckill_backend.model.User;
import com.example.seckill_backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @PostMapping("/login")
    public ApiResponse<Boolean> login(@Valid @RequestBody UserLoginRequest request, HttpSession session) {
        String username = request.getUsername();
        String password = request.getPassword();

        if ("admin".equals(username) && "seckill".equals(password)) {
            session.setAttribute("USER_ID", 0L);
            session.setAttribute("ROLE", "ADMIN");
            return ApiResponse.success(true);
        }

        User user = userService.login(username, password);
        if (user != null) {
            session.setAttribute("USER_ID", user.getId());
            session.setAttribute("ROLE", "ADMIN");
        }
        return ApiResponse.success(user != null);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ApiResponse.success(null);
    }
}
