package com.blogchi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ArticleDTO {

    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题最长 100 个字符")
    private String title;

    @Size(max = 500, message = "摘要最长 500 个字符")
    private String summary;

    @NotBlank(message = "正文不能为空")
    private String content;

    @NotNull(message = "必须选择分类")
    private Long categoryId;

    /**
     * 0 草稿，1 已发布。
     */
    @NotNull(message = "必须指定文章状态")
    private Integer status;

    private List<Long> tagIds;
}
