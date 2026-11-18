package com.blogchi.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签，同样按 user_id 归属到某个博主。
 */
@Data
@Table("tag")
public class Tag {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long userId;

    private String name;

    @Column(onInsertValue = "now()")
    private LocalDateTime createTime;

    @Column(onInsertValue = "now()", onUpdateValue = "now()")
    private LocalDateTime updateTime;
}
