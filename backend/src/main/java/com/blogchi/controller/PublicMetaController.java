package com.blogchi.controller;

import com.blogchi.common.Result;
import com.blogchi.service.CategoryService;
import com.blogchi.service.TagService;
import com.blogchi.vo.CategoryVO;
import com.blogchi.vo.TagVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前台筛选用到的分类和标签。
 */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicMetaController {

    private final CategoryService categoryService;
    private final TagService tagService;

    @GetMapping("/categories")
    public Result<List<CategoryVO>> categories() {
        return Result.ok(categoryService.publicList());
    }

    @GetMapping("/tags")
    public Result<List<TagVO>> tags() {
        return Result.ok(tagService.publicList());
    }
}
