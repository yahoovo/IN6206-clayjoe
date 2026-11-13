package com.clayjoe.backend.dto;

import com.clayjoe.backend.entity.PaymentMethod;
import com.clayjoe.backend.entity.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record SimulatePaymentRequest(
        @NotBlank(message = "请求编号不能为空")
        @Pattern(
                regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
                message = "请求编号必须是UUID格式"
        )
        String requestKey,

        @NotNull(message = "请选择支付方式")
        PaymentMethod paymentMethod,

        @NotNull(message = "请选择模拟支付结果")
        PaymentStatus result
) {}