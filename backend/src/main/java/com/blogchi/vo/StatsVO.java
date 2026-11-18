package com.blogchi.vo;

import lombok.Data;

@Data
public class StatsVO {

    private long articleCount;

    private long publishedCount;

    private long draftCount;

    private long commentCount;

    private long viewCount;

    private long categoryCount;

    private long tagCount;
}
