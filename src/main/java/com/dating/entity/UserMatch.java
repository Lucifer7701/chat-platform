package com.dating.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserMatch {
    private Long id;
    private Long userId;
    private Long targetUserId;
    private Integer action; // 1喜欢 2不喜欢
    private Integer isMutual; // 0否 1是
    private LocalDateTime createdAt;
}