package com.univ.momcare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordDTO {
    @NotBlank(message = "请输入当前密码")
    private String oldPassword;
    @NotBlank(message = "请输入新密码")
    @Size(min = 8, max = 20, message = "新密码长度需 8-20 位")
    private String newPassword;
}
