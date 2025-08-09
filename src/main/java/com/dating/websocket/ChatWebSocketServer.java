// 8. WebSocket服务器端点
package com.dating.websocket;

import com.dating.dto.ChatMessageRequest;
import com.dating.dto.ChatMessageResponse;
import com.dating.entity.ChatMessage;
import com.dating.service.ChatService;
import com.dating.service.OnlineStatusService;
import com.dating.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

import static com.dating.service.OnlineStatusService.USER_SESSIONS;

@Slf4j
@Component
@ServerEndpoint("/ws/chat/{token}")
public class ChatWebSocketServer {

    private static ChatService chatService;
    private static JwtUtil jwtUtils;
    private static ObjectMapper objectMapper;
    private static OnlineStatusService onlineStatusService;

    // 存储用户连接
//    private static final ConcurrentHashMap<Long, Session> USER_SESSIONS = new ConcurrentHashMap<>();

    @Autowired
    public void setChatService(ChatService chatService) {
        ChatWebSocketServer.chatService = chatService;
    }

    @Autowired
    public void setJwtUtils(JwtUtil jwtUtils) {
        ChatWebSocketServer.jwtUtils = jwtUtils;
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        ChatWebSocketServer.objectMapper = objectMapper;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        try {
            // 验证token并获取用户ID
            Long userId = Long.valueOf(jwtUtils.getUserIdFromToken(token));
            if (userId != null) {
                USER_SESSIONS.put(userId, session);
                log.info("用户{}连接WebSocket成功", userId);
            } else {
                session.close();
                log.warn("WebSocket连接失败：token无效");
            }
        } catch (Exception e) {
            log.error("WebSocket连接异常", e);
            try {
                session.close();
            } catch (IOException ioException) {
                log.error("关闭WebSocket连接异常", ioException);
            }
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            // 使用Jackson解析消息
            ChatMessageRequest request = objectMapper.readValue(message, ChatMessageRequest.class);

            // 获取发送者ID
            Long fromUserId = getUserIdBySession(session);
            if (fromUserId == null) {
                return;
            }

            // 保存消息到数据库
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setFromUserId(fromUserId);
            chatMessage.setToUserId(request.getToUserId());
            chatMessage.setMessageType(request.getMessageType());
            chatMessage.setContent(request.getContent());
            chatMessage.setMediaUrl(request.getMediaUrl());

            chatService.saveMessage(chatMessage);

            // 发送给接收者
            Session toUserSession = USER_SESSIONS.get(request.getToUserId());
            if (toUserSession != null && toUserSession.isOpen()) {
                ChatMessageResponse response = new ChatMessageResponse();
                response.setFromUserId(fromUserId);
                response.setToUserId(request.getToUserId());
                response.setMessageType(request.getMessageType());
                response.setContent(request.getContent());
                response.setMediaUrl(request.getMediaUrl());
                response.setCreatedAt(chatMessage.getCreatedAt());

                // 使用Jackson序列化响应
                String responseJson = objectMapper.writeValueAsString(response);
                toUserSession.getBasicRemote().sendText(responseJson);
            }

            log.info("消息发送成功：{} -> {}", fromUserId, request.getToUserId());

        } catch (Exception e) {
            log.error("处理WebSocket消息异常", e);
        }
    }

    @OnClose
    public void onClose(Session session) {
        Long userId = getUserIdBySession(session);
        if (userId != null) {
            USER_SESSIONS.remove(userId);
            log.info("用户{}断开WebSocket连接", userId);
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket连接异常", error);
    }

    private Long getUserIdBySession(Session session) {
        // 通过在线状态服务查找用户ID
        return onlineStatusService.getOnlineUsers().stream()
                .filter(userId -> session.equals(onlineStatusService.getUserSession(userId)))
                .findFirst()
                .orElse(null);
    }
}