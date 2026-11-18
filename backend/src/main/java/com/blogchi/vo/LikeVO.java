package com.blogchi.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LikeVO {

    private boolean liked;

    private long likeCount;
}
