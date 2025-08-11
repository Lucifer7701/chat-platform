package com.dating.entity;

import lombok.Data;
import java.util.Date;

@Data
public class UserAuth {
    private Long id;
    private Long userId;
    private String phone;
    private String password;
    private Integer loginType;
    private String thirdPartyId;
    private Date lastLoginTime;
    private String loginIp;
    private Integer loginCount;
    private Integer status;
    private Date createdAt;
    private Date updatedAt;
}