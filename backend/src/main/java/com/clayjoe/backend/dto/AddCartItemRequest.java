package com.clayjoe.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddCartItemRequest(
        @NotNull(message = "商品编号不能为空")
        @Min(value = 1, message = "商品编号必须大于0")
        Long productId,

        @NotNull(message = "数量不能为空")
        @Min(value = 1, message = "数量必须至少为1")
        Integer quantity
) {}