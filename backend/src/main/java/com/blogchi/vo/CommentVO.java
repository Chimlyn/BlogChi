package com.blogchi.vo;

import lombok.Data;

@Data
public class CommentVO {

    private Long id;

    private Long articleId;

    private String articleTitle;

    private String nickname;

    private String content;

    private String createTime;
}
