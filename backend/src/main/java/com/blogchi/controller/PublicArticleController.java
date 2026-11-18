package com.blogchi.controller;

import com.blogchi.common.PageResult;
import com.blogchi.common.Result;
import com.blogchi.service.ArticleService;
import com.blogchi.vo.ArticleDetailVO;
import com.blogchi.vo.ArticleVO;
import com.blogchi.vo.ArchiveVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 访客浏览文章：列表、搜索、筛选、详情、热门、归档。
 */
@RestController
@RequestMapping("/api/public/articles")
@RequiredArgsConstructor
public class PublicArticleController {

    private final ArticleService articleService;

    /**
     * 分页列表，支持关键词、分类、标签三种筛选条件自由组合。
     */
    @GetMapping
    public Result<PageResult<ArticleVO>> list(@RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int size,
                                              @RequestParam(required = false) String keyword,
                                              @RequestParam(required = false) Long categoryId,
                                              @RequestParam(required = false) Long tagId) {
        return Result.ok(articleService.publicList(page, size, keyword, categoryId, tagId));
    }

    /**
     * 热门文章：按阅读量倒序取前 N 篇。
     */
    @GetMapping("/hot")
    public Result<List<ArticleVO>> hot(@RequestParam(defaultValue = "5") int limit) {
        return Result.ok(articleService.hot(limit));
    }

    /**
     * 归档：按年月统计已发布文章数量。
     */
    @GetMapping("/archives")
    public Result<List<ArchiveVO>> archives() {
        return Result.ok(articleService.archives());
    }

    /**
     * 详情。visitorKey 由前端生成，用来判断当前访客是否已经点过赞。
     */
    @GetMapping("/{id}")
    public Result<ArticleDetailVO> detail(@PathVariable Long id,
                                          @RequestParam(required = false) String visitorKey) {
        return Result.ok(articleService.publicDetail(id, visitorKey));
    }
}
