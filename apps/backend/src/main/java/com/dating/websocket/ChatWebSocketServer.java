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
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.dating.service.OnlineStatusService.USER_SESSIONS;

@Slf4j
@Component
@ServerEndpoint("/ws/chat/{token}")
public class ChatWebSocketServer {


    @Autowired
    private ChatService chatService;
    @Autowired
    private JwtUtil jwtUtils;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private OnlineStatusService onlineStatusService;

    // 超时与心跳配置
    private static final long IDLE_TIMEOUT_MS = Duration.ofMinutes(5).toMillis();
    private static final long SEND_TIMEOUT_MS = Duration.ofSeconds(10).toMillis();
    private static final long PING_INTERVAL_SEC = 30;

    // 心跳调度器（服务器主动 ping）
    private static final ScheduledExecutorService PING_SCHEDULER =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "ws-ping");
                t.setDaemon(true);
                return t;
            });

    static {
        PING_SCHEDULER.scheduleAtFixedRate(() -> {
            USER_SESSIONS.forEach((uid, sess) -> {
                try {
                    if (sess != null && sess.isOpen()) {
                        // 轻量心跳，避免阻塞 I/O 线程
                        sess.getAsyncRemote().sendText("PING");
                    }
                } catch (Throwable t) {
                    try {
                        if (sess != null && sess.isOpen()) {
                            sess.close();
                        }
                    } catch (Exception ignore) {
                    }
                }
            });
        }, PING_INTERVAL_SEC, PING_INTERVAL_SEC, TimeUnit.SECONDS);
    }


    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        try {
            // 验证token并获取用户ID
            Long userId = Long.valueOf(jwtUtils.getUserIdFromToken(token));
            if (userId != null) {
                // 设置超时与发送配置
                session.setMaxIdleTimeout(IDLE_TIMEOUT_MS);
                session.getAsyncRemote().setSendTimeout(SEND_TIMEOUT_MS);
                // 在会话上记录 userId 便于反查
                session.getUserProperties().put("userId", userId);

                // 记录在线用户
                onlineStatusService.addUser(userId, session);
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
            Session toUserSession = onlineStatusService.getUserSession(request.getToUserId());
            if (toUserSession != null && toUserSession.isOpen()) {
                ChatMessageResponse response = new ChatMessageResponse();
                response.setFromUserId(fromUserId);
                response.setToUserId(request.getToUserId());
                response.setMessageType(request.getMessageType());
                response.setContent(request.getContent());
                response.setMediaUrl(request.getMediaUrl());
                response.setCreatedAt(chatMessage.getCreatedAt());

                // 使用Jackson序列化响应（异步发送）
                String responseJson = objectMapper.writeValueAsString(response);
                toUserSession.getAsyncRemote().sendText(responseJson);
            }

            log.info("消息发送成功：{} -> {}", fromUserId, request.getToUserId());

        } catch (Exception e) {
            log.error("处理WebSocket消息异常", e);
        }
    }

    @OnClose
    public void onClose(Session session) {
        try {
            Long userId = getUserIdBySession(session);
            if (userId != null) {
                onlineStatusService.removeUser(userId);
                log.info("用户{}断开WebSocket连接", userId);
            } else {
                // 兜底：按会话反向清理
                USER_SESSIONS.entrySet().removeIf(e -> e.getValue() == session);
            }
            session.getUserProperties().remove("userId");
            if (session.isOpen()) {
                session.close();
            }
            log.info("WebSocket连接关闭，已清理关联资源");
        } catch (Exception e) {
            log.error("关闭WebSocket连接清理异常", e);
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket连接异常", error);
        try {
            Long userId = getUserIdBySession(session);
            if (userId != null) {
                onlineStatusService.removeUser(userId);
            } else {
                USER_SESSIONS.entrySet().removeIf(e -> e.getValue() == session);
            }
            session.getUserProperties().remove("userId");
            if (session.isOpen()) {
                session.close();
            }
            log.info("WebSocket异常后已清理关联资源");
        } catch (Exception e) {
            log.error("WebSocket异常清理失败", e);
        }
    }

    private Long getUserIdBySession(Session session) {
        Object v = session.getUserProperties().get("userId");
        return v == null ? null : Long.valueOf(v.toString());
    }
}