package com.blogchi.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 博主账号。表名用 blog_user，避开 MySQL 里的 user 关键字。
 */
@Data
@Table("blog_user")
public class User {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String username;

    private String nickname;

    /**
     * BCrypt 加密后的密文，绝不返回给前端。
     */
    private String password;

    private String bio;

    @Column(onInsertValue = "now()")
    private LocalDateTime createTime;

    @Column(onInsertValue = "now()", onUpdateValue = "now()")
    private LocalDateTime updateTime;
}
