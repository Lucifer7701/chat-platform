// 5. 聊天消息服务
package com.dating.service;

import com.dating.entity.ChatMessage;
import com.dating.entity.UserMatch;
import com.dating.mapper.ChatMessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChatMessageService {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private DataIntegrityService dataIntegrityService;

    @Autowired
    private MatchService matchService;

    /**
     * 发送消息
     */
    @Transactional
    public boolean sendMessage(ChatMessage message) {
        // 验证发送和接收用户都存在且不是同一个用户
        if (!dataIntegrityService.validateTwoDifferentUsers(
                message.getFromUserId(), message.getToUserId())) {
            throw new IllegalArgumentException("用户不存在、状态异常或尝试给自己发消息");
        }

        // 验证两个用户是否互相喜欢（可选验证，根据业务需求）
        List<UserMatch> mutualMatches = matchService.getMutualMatches(message.getFromUserId());
        boolean canChat = mutualMatches.stream()
                .anyMatch(match -> match.getTargetUserId().equals(message.getToUserId()));

        if (!canChat) {
            throw new IllegalArgumentException("只有互相喜欢的用户才能聊天");
        }

        try {
            return chatMessageMapper.insert(message) > 0;
        } catch (Exception e) {
            throw new RuntimeException("发送消息失败", e);
        }
    }

    /**
     * 获取两个用户之间的聊天记录
     */
    public List<ChatMessage> getChatMessages(Long userId1, Long userId2, Integer page, Integer size) {
        if (!dataIntegrityService.validateTwoDifferentUsers(userId1, userId2)) {
            return null;
        }

        int offset = (page - 1) * size;
        return chatMessageMapper.findBetweenUsers(userId1, userId2, offset, size);
    }

    /**
     * 标记消息为已读
     */
    @Transactional
    public boolean markAsRead(Long userId, Long fromUserId) {
        if (!dataIntegrityService.validateTwoDifferentUsers(userId, fromUserId)) {
            throw new IllegalArgumentException("用户不存在或状态异常");
        }

        try {
            return chatMessageMapper.markAsRead(userId, fromUserId) > 0;
        } catch (Exception e) {
            throw new RuntimeException("标记消息已读失败", e);
        }
    }

    /**
     * 删除用户相关的所有聊天消息（用户注销时调用）
     */
    @Transactional
    public void deleteChatMessagesByUserId(Long userId) {
        chatMessageMapper.deleteByUserId(userId);
    }
}