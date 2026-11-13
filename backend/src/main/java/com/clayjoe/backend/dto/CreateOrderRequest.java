package com.clayjoe.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateOrderRequest(
        @NotBlank(message = "请求编号不能为空")
        @Pattern(
                regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
                message = "请求编号必须是UUID格式"
        )
        String requestKey,

        @NotBlank(message = "请填写收件人")
        @Size(max = 100, message = "收件人不能超过100个字符")
        String recipientName,

        @NotBlank(message = "请填写联系电话")
        @Size(max = 30, message = "联系电话不能超过30个字符")
        String recipientPhone,

        @NotBlank(message = "请填写收货地址")
        @Size(max = 500, message = "收货地址不能超过500个字符")
        String shippingAddress,

        @NotBlank(message = "请选择结算币种")
        @Pattern(regexp = "CNY|SGD", message = "仅支持CNY或SGD")
        String currency,

        @NotBlank(message = "请选择购物地区")
        @Pattern(regexp = "CN|SG", message = "购物地区仅支持CN或SG")
        String market,

        @NotBlank(message = "请选择收货国家或地区")
        @Pattern(regexp = "CN|SG", message = "收货国家仅支持CN或SG")
        String shippingCountry
) {}