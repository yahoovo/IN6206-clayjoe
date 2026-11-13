package com.clayjoe.backend.dto;

import com.clayjoe.backend.entity.PaymentMethod;
import com.clayjoe.backend.entity.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        Long id,
        Long orderId,
        PaymentMethod paymentMethod,
        PaymentStatus status,
        BigDecimal amount,
        String currency,
        Instant createdAt,
        boolean simulated
) {}