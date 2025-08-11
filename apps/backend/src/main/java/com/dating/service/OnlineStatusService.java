// 8. 在线状态管理服务
package com.dating.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.websocket.Session;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OnlineStatusService {

    // 存储用户连接
    public static final ConcurrentHashMap<Long, Session> USER_SESSIONS = new ConcurrentHashMap<>();

    public void addUser(Long userId, Session session) {
        USER_SESSIONS.put(userId, session);
        log.info("用户{}上线", userId);
    }

    public void removeUser(Long userId) {
        USER_SESSIONS.remove(userId);
        log.info("用户{}下线", userId);
    }

    public boolean isUserOnline(Long userId) {
        Session session = USER_SESSIONS.get(userId);
        return session != null && session.isOpen();
    }

    public Set<Long> getOnlineUsers() {
        return USER_SESSIONS.entrySet().stream()
                .filter(entry -> entry.getValue().isOpen())
                .map(ConcurrentHashMap.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public List<Long> getOnlineUsersFromList(List<Long> userIds) {
        return userIds.stream()
                .filter(this::isUserOnline)
                .collect(Collectors.toList());
    }

    public Session getUserSession(Long userId) {
        return USER_SESSIONS.get(userId);
    }

    public int getOnlineCount() {
        return (int) USER_SESSIONS.entrySet().stream()
                .filter(entry -> entry.getValue().isOpen())
                .count();
    }

    // 清理无效连接
    public void cleanInactiveSessions() {
        USER_SESSIONS.entrySet().removeIf(entry -> {
            Session session = entry.getValue();
            boolean isInactive = session == null || !session.isOpen();
            if (isInactive) {
                log.info("清理用户{}的无效连接", entry.getKey());
            }
            return isInactive;
        });
    }
}