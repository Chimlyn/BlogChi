package com.blogchi.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * JWT 工具：负责签发和校验 token。
 */
@Component
public class JwtUtil {

    public static final String CLAIM_UID = "uid";
    public static final String CLAIM_USERNAME = "username";

    @Value("${blog.jwt.secret}")
    private String secret;

    @Value("${blog.jwt.expire-days}")
    private long expireDays;

    private Algorithm algorithm;

    /**
     * Bean 初始化后只创建一次 Algorithm，避免每次请求重复构造。
     */
    @PostConstruct
    public void init() {
        this.algorithm = Algorithm.HMAC256(secret);
    }

    /**
     * 签发 token：payload 里只放用户 id 和用户名，不放密码等敏感信息。
     */
    public String createToken(Long uid, String username) {
        Instant now = Instant.now();
        Instant expire = now.plus(Duration.ofDays(expireDays));
        return JWT.create()
                .withClaim(CLAIM_UID, uid)
                .withClaim(CLAIM_USERNAME, username)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(expire))
                .sign(algorithm);
    }

    /**
     * 校验并解析 token；签名不对或过期都会抛异常。
     */
    public DecodedJWT parse(String token) {
        return JWT.require(algorithm).build().verify(token);
    }
}
