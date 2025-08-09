// 3. ChatContact DTO
package com.dating.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatContact {
    private Long contactUserId;
    private String nickname;
    private String avatar;
    private String lastMessage;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastMessageTime;

    private Integer unreadCount;
}
