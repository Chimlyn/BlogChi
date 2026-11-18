package com.blogchi.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章。content 存 Markdown 原文，渲染交给前端。
 */
@Data
@Table("article")
public class Article {

    /**
     * 草稿。
     */
    public static final int STATUS_DRAFT = 0;

    /**
     * 已发布。
     */
    public static final int STATUS_PUBLISHED = 1;

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long userId;

    private Long categoryId;

    private String title;

    /**
     * 摘要，列表页展示，不传则前端截取正文。
     */
    private String summary;

    private String content;

    private Integer status;

    private Integer viewCount;

    @Column(onInsertValue = "now()")
    private LocalDateTime createTime;

    @Column(onInsertValue = "now()", onUpdateValue = "now()")
    private LocalDateTime updateTime;
}
