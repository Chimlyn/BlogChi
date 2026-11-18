package com.blogchi.security;

import com.blogchi.common.Code;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 过滤器链里的异常不会走 @RestControllerAdvice，所以这里手写 JSON 返回。
 */
@Component
public class ResponseWriter {

    public void write(HttpServletResponse response, Code code) throws IOException {
        response.setStatus(code.getCode());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":" + code.getCode()
                + ",\"message\":\"" + code.getMessage() + "\",\"data\":null}");
    }
}
