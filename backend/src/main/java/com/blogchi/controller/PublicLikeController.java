package com.blogchi.controller;

import com.blogchi.common.Result;
import com.blogchi.dto.LikeDTO;
import com.blogchi.service.ArticleService;
import com.blogchi.vo.LikeVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 点赞接口。再次调用就是取消点赞。
 */
@RestController
@RequestMapping("/api/public/articles/{id}/like")
@RequiredArgsConstructor
public class PublicLikeController {

    private final ArticleService articleService;

    @PostMapping
    public Result<LikeVO> toggle(@PathVariable Long id, @Valid @RequestBody LikeDTO dto) {
        return Result.ok(articleService.toggleLike(id, dto.getVisitorKey()));
    }
}
