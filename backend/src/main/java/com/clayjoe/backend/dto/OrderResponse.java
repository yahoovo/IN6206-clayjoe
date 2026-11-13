package com.clayjoe.backend.dto;

import com.clayjoe.backend.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        OrderStatus status,
        BigDecimal totalAmount,
        String currency,
        Instant createdAt,
        String recipientName,
        String recipientPhone,
        String shippingAddress,
        String shippingCarrier,
        String trackingNumber,
        Instant shippedAt,
        List<Item> items,
        String market,
        String shippingCountry
) {
    public record Item(
            Long productId,
            String name,
            String nameEn,
            BigDecimal unitPrice,
            Integer quantity,
            BigDecimal subtotal
    ) {}
}