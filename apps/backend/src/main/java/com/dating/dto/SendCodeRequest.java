package com.dating.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class SendCodeRequest {
    @NotBlank(message = "手机号不能为空")
    private String phone;
}
