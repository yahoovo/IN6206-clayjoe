package com.clayjoe.backend.dto;

import java.math.BigDecimal;

public record CartItemResponse(
        Long id,
        Long productId,
        String name,
        String imageUrl,
        BigDecimal price,
        Integer quantity,
        BigDecimal subtotal,
        Integer stock,
        Boolean active,
        String currency,
        String nameEn,
        java.util.Map<String, BigDecimal> prices,
        BigDecimal sgdToCnyRate
) {}