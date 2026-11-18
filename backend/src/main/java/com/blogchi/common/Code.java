package com.blogchi.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 统一业务状态码。前端只需要判断 code 是否为 200。
 */
@Getter
@RequiredArgsConstructor
public enum Code {
    OK(200, "成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录，请先登录"),
    TOKEN_EXPIRED(401, "登录已过期，请重新登录"),
    FORBIDDEN(403, "无权访问该数据"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "数据已存在"),
    ERROR(500, "服务器内部错误");

    private final int code;
    private final String message;
}
