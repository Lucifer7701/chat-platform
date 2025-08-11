package com.dating.entity;

import lombok.Data;
import java.time.LocalDateTime;

// 举报实体
@Data
public class Report {
    private Long id;
    private Long reporterId;
    private Long reportedUserId;
    private Integer reportType;
    private String reason;
    private Integer status; // 1待处理 2已处理 3已忽略
    private LocalDateTime createdAt;
}