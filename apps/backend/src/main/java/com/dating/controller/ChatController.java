// 10. 聊天控制器
package com.dating.controller;

import com.dating.service.OnlineStatusService;
import com.dating.util.Result;
import com.dating.dto.ChatContact;
import com.dating.entity.ChatMessage;
import com.dating.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.dating.util.JwtUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private JwtUtil jwtUtils;

    @Autowired
    private OnlineStatusService onlineStatusService;

    // 获取聊天历史记录
    @GetMapping("/history/{targetUserId}")
    public Result<List<ChatMessage>> getChatHistory(
            @PathVariable Long targetUserId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            HttpServletRequest request) {

        Long userId = jwtUtils.getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error("未登录");
        }

        List<ChatMessage> messages = chatService.getChatHistory(userId, targetUserId, page, size);
        return Result.success(messages);
    }

    // 标记消息为已读
    @PostMapping("/read/{fromUserId}")
    public Result<Void> markAsRead(@PathVariable Long fromUserId, HttpServletRequest request) {
        Long userId = jwtUtils.getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error("未登录");
        }

        chatService.markAsRead(fromUserId, userId);
        return Result.success();
    }

    // 获取未读消息数量
    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount(HttpServletRequest request) {
        Long userId = jwtUtils.getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error("未登录");
        }

        int count = chatService.getUnreadCount(userId);
        return Result.success(count);
    }

    // 获取聊天联系人列表（增强版，包含在线状态）
    @GetMapping("/contacts")
    public Result<List<ChatContactWithStatus>> getChatContacts(HttpServletRequest request) {
        Long userId = jwtUtils.getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error("未登录");
        }

        List<ChatContact> contacts = chatService.getChatContacts(userId);

        // 添加在线状态信息
        List<ChatContactWithStatus> contactsWithStatus = contacts.stream()
                .map(contact -> {
                    ChatContactWithStatus contactWithStatus = new ChatContactWithStatus();
                    contactWithStatus.setContactUserId(contact.getContactUserId());
                    contactWithStatus.setNickname(contact.getNickname());
                    contactWithStatus.setAvatar(contact.getAvatar());
                    contactWithStatus.setLastMessage(contact.getLastMessage());
                    contactWithStatus.setLastMessageTime(contact.getLastMessageTime());
                    contactWithStatus.setUnreadCount(contact.getUnreadCount());
                    contactWithStatus.setOnline(onlineStatusService.isUserOnline(contact.getContactUserId()));
                    return contactWithStatus;
                })
                .collect(java.util.stream.Collectors.toList());

        return Result.success(contactsWithStatus);
    }

    // 内部类：带在线状态的聊天联系人
    public static class ChatContactWithStatus extends ChatContact {
        private Boolean online;

        public Boolean getOnline() { return online; }
        public void setOnline(Boolean online) { this.online = online; }
    }
}
