package com.example.seckill_backend.common;

public final class AuthContext {

    private static final ThreadLocal<AuthUser> HOLDER = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void set(AuthUser user) {
        HOLDER.set(user);
    }

    public static AuthUser get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }

    public static Long requireUserId() {
        AuthUser user = get();
        if (user == null || user.userId() == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        return user.userId();
    }

    public static String requireRole() {
        AuthUser user = get();
        if (user == null || user.role() == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        return user.role();
    }

    public record AuthUser(Long userId, String role) {
    }
}
