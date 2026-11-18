package com.blogchi.security;

import lombok.Builder;
import lombok.Data;

/**
 * 登录用户在 Spring Security 上下文里的载体，只存查库/鉴别所需的最小信息。
 */
@Data
@Builder
public class LoginUser {

    private Long id;

    private String username;
}
