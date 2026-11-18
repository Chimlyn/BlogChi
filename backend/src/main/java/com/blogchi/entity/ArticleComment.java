package com.blogchi.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章评论。访客不登录，只留昵称和内容。
 */
@Data
@Table("article_comment")
public class ArticleComment {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long articleId;

    private String nickname;

    private String content;

    @Column(onInsertValue = "now()")
    private LocalDateTime createTime;
}
