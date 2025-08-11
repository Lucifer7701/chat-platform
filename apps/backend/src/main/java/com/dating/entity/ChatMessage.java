package com.dating.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatMessage {
    private Long id;
    private Long fromUserId;
    private Long toUserId;
    private Integer messageType; // 1文本 2图片 3语音
    private String content;
    private String mediaUrl;
    private Integer isRead; // 0未读 1已读
    private LocalDateTime createdAt;

    // 扩展字段，不存数据库
    private String fromUserNickname;
    private String fromUserAvatar;
}