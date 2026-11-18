package com.blogchi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 分类和标签都只需要一个名字，共用一个 DTO。
 */
@Data
public class NameDTO {

    @NotBlank(message = "名称不能为空")
    @Size(max = 20, message = "名称最长 20 个字符")
    private String name;
}
