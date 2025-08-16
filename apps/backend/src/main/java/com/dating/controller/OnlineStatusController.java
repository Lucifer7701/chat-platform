package com.dating.controller;

import com.dating.util.Result;
import com.dating.service.OnlineStatusService;
import com.dating.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/online")
@CrossOrigin(origins = "*")
public class OnlineStatusController {

    @Autowired
    private OnlineStatusService onlineStatusService;

    @Autowired
    private JwtUtil jwtUtils;

    // 检查用户是否在线
    @GetMapping("/status/{userId}")
    public Result<Boolean> isUserOnline(@PathVariable Long userId) {
        boolean online = onlineStatusService.isUserOnline(userId);
        return Result.success(online);
    }

    // 批量检查用户在线状态
    @PostMapping("/status/batch")
    public Result<List<Long>> getOnlineUsersFromList(@RequestBody List<Long> userIds, HttpServletRequest request) {
        Long currentUserId = jwtUtils.getUserIdFromRequest(request);
        if (currentUserId == null) {
            return Result.error("未登录");
        }

        List<Long> onlineUsers = onlineStatusService.getOnlineUsersFromList(userIds);
        return Result.success(onlineUsers);
    }

    // 获取当前在线用户总数
    @GetMapping("/count")
    public Result<Integer> getOnlineCount() {
        int count = onlineStatusService.getOnlineCount();
        return Result.success(count);
    }

    // 获取所有在线用户ID列表（管理员接口）
    @GetMapping("/users")
    public Result<Set<Long>> getOnlineUsers(HttpServletRequest request) {
        Long userId = jwtUtils.getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error("未登录");
        }

        // 这里可以添加权限验证，只允许管理员查看
        Set<Long> onlineUsers = onlineStatusService.getOnlineUsers();
        return Result.success(onlineUsers);
    }
}

