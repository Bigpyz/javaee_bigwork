package com.example.seckill_backend.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class MockAuthInterceptor implements HandlerInterceptor {

    private static final String HEADER_USER_ID = "X-USER-ID";
    private static final String HEADER_ROLE = "X-ROLE";
    private static final String SESSION_USER_ID = "USER_ID";
    private static final String SESSION_ROLE = "ROLE";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String method = request.getMethod();
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        Long userId = null;
        String role = null;

        HttpSession session = request.getSession(false);
        if (session != null) {
            Object sessionUserId = session.getAttribute(SESSION_USER_ID);
            if (sessionUserId instanceof Number n) {
                userId = n.longValue();
            } else if (sessionUserId instanceof String s && !s.isBlank()) {
                try {
                    userId = Long.parseLong(s);
                } catch (NumberFormatException ignored) {
                    userId = null;
                }
            }

            Object sessionRole = session.getAttribute(SESSION_ROLE);
            if (sessionRole != null) {
                role = String.valueOf(sessionRole);
            }
        }

        String userIdStr = request.getHeader(HEADER_USER_ID);
        if (userId == null && userIdStr != null && !userIdStr.isBlank()) {
            try {
                userId = Long.parseLong(userIdStr);
            } catch (NumberFormatException e) {
                throw new BizException(ErrorCode.BAD_REQUEST, "X-USER-ID 格式错误");
            }
        }

        String headerRole = request.getHeader(HEADER_ROLE);
        if (role == null || role.isBlank()) {
            role = headerRole;
        }

        if (role == null || role.isBlank()) {
            role = "USER";
        }

        AuthContext.set(new AuthContext.AuthUser(userId, role));

        String path = request.getRequestURI();
        method = request.getMethod();

        if (path.startsWith("/api/products") && isWriteMethod(method)) {
            requireAdmin();
        }

        if (path.startsWith("/api/activities") && isWriteMethod(method)) {
            if (!path.contains("/record-visit")) {
                requireAdmin();
            }
        }

        if (path.startsWith("/api/orders/process-expired") || path.startsWith("/api/orders/payment-callback")) {
            requireAdmin();
        }

        if (path.startsWith("/api/debug")) {
            requireAdmin();
        }

        if (path.startsWith("/api/orders") && !path.startsWith("/api/orders/payment-callback")) {
            if (path.contains("/status/")) {
                requireAdmin();
            } else {
                requireUser();
            }
        }

        if (path.startsWith("/api/upload")) {
            requireUser();
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }

    private boolean isWriteMethod(String method) {
        return "POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method);
    }

    private void requireUser() {
        if (AuthContext.get() == null || AuthContext.get().userId() == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
    }

    private void requireAdmin() {
        String role = AuthContext.requireRole();
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
    }
}
