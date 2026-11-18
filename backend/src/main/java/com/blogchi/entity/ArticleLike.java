package com.blogchi.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 点赞记录。访客不登录，用前端生成的 visitorKey 做去重。
 */
@Data
@Table("article_like")
public class ArticleLike {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long articleId;

    private String visitorKey;

    @Column(onInsertValue = "now()")
    private LocalDateTime createTime;
}
