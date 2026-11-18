package com.blogchi.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenVO {

    private String token;

    private UserVO user;
}
