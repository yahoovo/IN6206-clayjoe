package com.clayjoe.backend.dto;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        Long userId,
        Long orderId,
        String titleZh,
        String titleEn,
        String contentZh,
        String contentEn,
        Instant createdAt,
        Instant readAt
) {
}