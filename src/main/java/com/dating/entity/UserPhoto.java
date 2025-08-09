// 用户照片实体
package com.dating.entity;

import lombok.Data;
import java.time.LocalDateTime;
@Data
public class UserPhoto {
    private Long id;
    private Long userId;
    private String photoUrl;
    private Integer isAvatar; // 0否 1是
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createdAt;
}