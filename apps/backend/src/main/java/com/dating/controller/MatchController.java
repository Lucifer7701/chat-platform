// 2. 匹配控制器
package com.dating.controller;

import com.dating.service.MatchService;
import com.dating.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/match")
@CrossOrigin
public class MatchController {

    @Autowired
    private MatchService matchService;

    /**
     * 喜欢用户
     */
    @PostMapping("/like")
    public Result likeUser(@RequestAttribute("userId") Long userId,
                           @RequestParam Long targetUserId) {
        try {
            matchService.userMatch(userId, targetUserId, 1);
            return Result.success("操作成功");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("操作失败");
        }
    }

    /**
     * 跳过用户
     */
    @PostMapping("/pass")
    public Result passUser(@RequestAttribute("userId") Long userId,
                           @RequestParam Long targetUserId) {
        try {
            matchService.userMatch(userId, targetUserId, 2);
            return Result.success("操作成功");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("操作失败");
        }
    }

    /**
     * 获取互相喜欢的用户列表
     */
    @GetMapping("/mutual")
    public Result getMutualMatches(@RequestAttribute("userId") Long userId) {
        try {
            return Result.success(matchService.getMutualMatches(userId));
        } catch (Exception e) {
            return Result.error("获取列表失败");
        }
    }
}