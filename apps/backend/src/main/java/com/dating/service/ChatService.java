// 9. 聊天服务类
package com.dating.service;

import com.dating.dto.ChatContact;
import com.dating.entity.ChatMessage;
import com.dating.mapper.ChatMessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    public ChatMessage saveMessage(ChatMessage chatMessage) {
        chatMessageMapper.insert(chatMessage);
        return chatMessage;
    }

    public List<ChatMessage> getChatHistory(Long userId1, Long userId2, Integer page, Integer size) {
        int offset = (page - 1) * size;
        return chatMessageMapper.getChatHistory(userId1, userId2, offset, size);
    }

    public void markAsRead(Long fromUserId, Long toUserId) {
        chatMessageMapper.markAsRead(fromUserId, toUserId);
    }

    public int getUnreadCount(Long userId) {
        return chatMessageMapper.getUnreadCount(userId);
    }

    public List<ChatContact> getChatContacts(Long userId) {
        return chatMessageMapper.getChatContacts(userId);
    }
}
