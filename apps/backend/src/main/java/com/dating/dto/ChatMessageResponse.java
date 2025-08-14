package com.dating.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessageResponse {
    private Long id;              // 服务器消息ID
    private String tempId;        // 客户端临时ID
    private Long fromUserId;
    private Long toUserId;
    private Integer messageType;
    private String content;
    private String mediaUrl;
    private String type;          // 消息类型: "message" | "ack" | "error"

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    // 扩展字段
    private String fromUserNickname;
    private String fromUserAvatar;
}