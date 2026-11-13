package com.clayjoe.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateCartQuantityRequest(
        @NotNull(message = "数量不能为空")
        @Min(value = 1, message = "数量必须至少为1")
        Integer quantity
) {}