package com.dating.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String phone;
    private String username;
    private String nickname;
    private String avatar;
    private Integer gender; // 1男 2女
    private LocalDate birthday;
    private String city;
    private String profession;
    private String introduction;
    private Integer status; // 1正常 2冻结 3注销
    private Integer realNameVerified;
    private BigDecimal latitude; // 纬度
    private BigDecimal longitude; // 经度
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}