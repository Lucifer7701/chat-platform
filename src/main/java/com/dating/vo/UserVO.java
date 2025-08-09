package com.dating.vo;

import lombok.Data;

@Data
public class UserVO {
    private Long id;
    private String phone;
    private String username;
    private String nickname;
    private String avatar;
    private Integer gender;
    private String city;
    private Integer realNameVerified;
    private Integer status;
}