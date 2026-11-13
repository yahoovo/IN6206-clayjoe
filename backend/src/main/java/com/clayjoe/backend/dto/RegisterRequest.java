package com.clayjoe.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "用户名不能为空")
        @Pattern(
                regexp = "^[a-zA-Z0-9_]{3,50}$",
                message = "用户名需为3至50位字母、数字或下划线"
        )
        String username,

        @NotBlank(message = "密码不能为空")
        @Size(min = 12, max = 72, message = "密码长度需为12至72个字符")
        String password

) {}