package com.blogchi.vo;

import lombok.Data;

import java.util.List;

/**
 * 文章列表项。列表只需要展示信息，不带 Markdown 正文。
 */
@Data
public class ArticleVO {

    private Long id;

    private String title;

    private String summary;

    private String authorName;

    private Long categoryId;

    private String categoryName;

    private List<String> tags;

    private Integer viewCount;

    private Integer status;

    private String createTime;
}
