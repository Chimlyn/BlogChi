package com.blogchi.security;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.blogchi.common.Code;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.PathContainer;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.io.IOException;
import java.util.List;

/**
 * 每个请求只执行一次：从 Authorization 头里取 token，解析成功后把用户放进 SecurityContext。
 * 解析失败不抛异常，只把原因记在 request 上，交给 SecurityConfig 的 entryPoint 统一返回 JSON。
 */
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    public static final String AUTH_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String ERROR_ATTR = "jwt.error";

    private static final PathPattern PUBLIC_PATH =
            new PathPatternParser().parse("/api/public/**");

    private final JwtUtil jwtUtil;

    /**
     * 公开接口不需要校验 token，直接放行。
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return PUBLIC_PATH.matches(PathContainer.parsePath(request.getServletPath()));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(AUTH_HEADER);
        if (!StringUtils.hasText(header) || !header.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            DecodedJWT jwt = jwtUtil.parse(header.substring(BEARER_PREFIX.length()));
            LoginUser loginUser = LoginUser.builder()
                    .id(jwt.getClaim(JwtUtil.CLAIM_UID).asLong())
                    .username(jwt.getClaim(JwtUtil.CLAIM_USERNAME).asString())
                    .build();
            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(loginUser, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (TokenExpiredException e) {
            request.setAttribute(ERROR_ATTR, Code.TOKEN_EXPIRED);
        } catch (Exception e) {
            request.setAttribute(ERROR_ATTR, Code.UNAUTHORIZED);
        }

        filterChain.doFilter(request, response);
    }
}
