package com.blogchi.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章与标签的中间表，多对多关系靠它维系。
 */
@Data
@Table("article_tag")
public class ArticleTag {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long articleId;

    private Long tagId;

    @Column(onInsertValue = "now()")
    private LocalDateTime createTime;
}
