package com.blogchi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 访客唯一标识，前端第一次访问时生成 uuid 存 localStorage。
 */
@Data
public class LikeDTO {

    @NotBlank(message = "visitorKey 不能为空")
    @Size(max = 64, message = "visitorKey 最长 64 个字符")
    private String visitorKey;
}
