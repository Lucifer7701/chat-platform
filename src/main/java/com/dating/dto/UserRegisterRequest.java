package com.dating.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
public class UserRegisterRequest {
    @NotBlank(message = "手机号不能为空")
    private String phone;

    @NotBlank(message = "密码不能为空")
    @Length(min = 6, max = 20, message = "密码长度为6-20位")
    private String password;

    @NotBlank(message = "验证码不能为空")
    private String smsCode;

    private String username;
    private String nickname;
    private Integer gender; // 1-男 2-女
}