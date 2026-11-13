package com.clayjoe.backend.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateProductStatusRequest(
        @NotNull(message = "必须指定上架状态")
        Boolean active
) {}