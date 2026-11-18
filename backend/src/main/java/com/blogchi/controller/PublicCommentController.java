package com.blogchi.controller;

import com.blogchi.common.PageResult;
import com.blogchi.common.Result;
import com.blogchi.dto.CommentDTO;
import com.blogchi.service.CommentService;
import com.blogchi.vo.CommentVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 访客评论：查看和发表，只填昵称 + 内容，不要求登录。
 */
@RestController
@RequestMapping("/api/public/articles/{articleId}/comments")
@RequiredArgsConstructor
public class PublicCommentController {

    private final CommentService commentService;

    @GetMapping
    public Result<PageResult<CommentVO>> list(@PathVariable Long articleId,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        return Result.ok(commentService.listByArticle(articleId, page, size));
    }

    @PostMapping
    public Result<Void> add(@PathVariable Long articleId,
                            @Valid @RequestBody CommentDTO dto) {
        commentService.add(articleId, dto);
        return Result.ok();
    }
}
