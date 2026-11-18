package com.blogchi.security;

import com.blogchi.common.Code;
import com.blogchi.exception.BizException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 取当前登录用户的便捷入口。所有 admin 接口最终都通过它拿 userId。
 */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static LoginUser get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser loginUser)) {
            throw new BizException(Code.UNAUTHORIZED);
        }
        return loginUser;
    }

    public static Long id() {
        return get().getId();
    }
}
