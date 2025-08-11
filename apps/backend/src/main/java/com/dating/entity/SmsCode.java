package com.dating.entity;

import lombok.Data;
import java.util.Date;

@Data
public class SmsCode {
    private Long id;
    private String phone;
    private String code;
    private Integer type;
    private Integer used;
    private Date expireTime;
    private String ip;
    private Date createdAt;
}