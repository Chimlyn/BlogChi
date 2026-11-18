package com.blogchi.vo;

import lombok.Data;

@Data
public class ArchiveVO {

    /**
     * 形如 2026-11。
     */
    private String month;

    private Long total;
}
