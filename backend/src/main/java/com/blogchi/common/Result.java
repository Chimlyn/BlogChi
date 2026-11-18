package com.blogchi.common;

import lombok.Data;

/**
 * 所有接口的统一返回体：code + message + data。
 */
@Data
public class Result<T> {

    private int code;
    private String message;
    private T data;

    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> ok() {
        return new Result<>(Code.OK.getCode(), Code.OK.getMessage(), null);
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(Code.OK.getCode(), Code.OK.getMessage(), data);
    }

    public static <T> Result<T> error(Code code) {
        return new Result<>(code.getCode(), code.getMessage(), null);
    }

    public static <T> Result<T> error(Code code, String message) {
        return new Result<>(code.getCode(), message, null);
    }
}
