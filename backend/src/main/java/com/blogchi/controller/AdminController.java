package com.blogchi.controller;

import com.blogchi.common.Result;
import com.blogchi.service.StatsService;
import com.blogchi.service.UserService;
import com.blogchi.vo.StatsVO;
import com.blogchi.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台通用的两个接口：当前登录人、数据概览。
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final StatsService statsService;

    @GetMapping("/me")
    public Result<UserVO> me() {
        return Result.ok(userService.current());
    }

    @GetMapping("/stats")
    public Result<StatsVO> stats() {
        return Result.ok(statsService.stats());
    }
}
