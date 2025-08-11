// 7. WebSocket消息DTO
package com.dating.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatMessageRequest {
    private Long toUserId;
    private Integer messageType;
    private String content;
    private String mediaUrl;
}