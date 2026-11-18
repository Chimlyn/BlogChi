package com.blogchi.vo;

import lombok.Data;

import java.util.List;

/**
 * 文章详情。content 是 Markdown 原文，由前端用 marked 渲染。
 */
@Data
public class ArticleDetailVO {

    private Long id;

    private String title;

    private String summary;

    private String content;

    private String authorName;

    private Long categoryId;

    private String categoryName;

    private List<Long> tagIds;

    private List<String> tags;

    private Integer viewCount;

    private Long likeCount;

    private boolean liked;

    private Integer status;

    private String createTime;
}
