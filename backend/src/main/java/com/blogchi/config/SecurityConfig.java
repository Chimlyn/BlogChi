package com.blogchi.config;

import com.blogchi.common.Code;
import com.blogchi.security.JwtFilter;
import com.blogchi.security.ResponseWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

/**
 * 无状态鉴权：关掉 session、csrf，登录靠 JwtFilter 塞进 SecurityContext。
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final ResponseWriter responseWriter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(configurer ->
                        configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 浏览器的跨域预检请求必须放行，否则前端报跨域
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 访客接口：注册、登录、文章浏览、评论、点赞
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        // 博主后台接口：必须携带有效 token
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                .exceptionHandling(exc -> exc
                        .authenticationEntryPoint(this::onUnauthorized)
                        .accessDeniedHandler((request, response, e) ->
                                responseWriter.write(response, Code.FORBIDDEN)))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * token 缺失、过期、被篡改都走这里，前端据此跳登录页。
     */
    private void onUnauthorized(HttpServletRequest request,
                                HttpServletResponse response,
                                AuthenticationException e) throws IOException {
        Object reason = request.getAttribute(JwtFilter.ERROR_ATTR);
        Code code = reason instanceof Code c ? c : Code.UNAUTHORIZED;
        responseWriter.write(response, code);
    }
}
