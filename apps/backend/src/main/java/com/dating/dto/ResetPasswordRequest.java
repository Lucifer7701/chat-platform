// ============= 重置密码请求对象 =============
package com.dating.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

@Data
public class ResetPasswordRequest {
    @NotBlank(message = "手机号不能为空")
    private String phone;

    @NotBlank(message = "验证码不能为空")
    private String smsCode;

    @NotBlank(message = "新密码不能为空")
    @Length(min = 6, max = 20, message = "密码长度为6-20位")
    private String newPassword;
}