package com.blogchi.exception;

import com.blogchi.common.Code;
import lombok.Getter;

/**
 * 业务异常：由 service 层主动抛出，交给全局异常处理器转成统一返回体。
 */
@Getter
public class BizException extends RuntimeException {

    private final Code code;

    public BizException(Code code) {
        super(code.getMessage());
        this.code = code;
    }

    public BizException(Code code, String message) {
        super(message);
        this.code = code;
    }
}
