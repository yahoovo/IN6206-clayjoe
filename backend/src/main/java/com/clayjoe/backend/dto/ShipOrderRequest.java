package com.clayjoe.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ShipOrderRequest(
        @NotBlank(message = "请填写物流公司")
        @Size(max = 100, message = "物流公司不能超过100个字符")
        String shippingCarrier,

        @NotBlank(message = "请填写运单号")
        @Size(max = 100, message = "运单号不能超过100个字符")
        String trackingNumber
) {}