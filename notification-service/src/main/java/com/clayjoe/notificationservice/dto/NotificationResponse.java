package com.clayjoe.notificationservice.dto;

import com.clayjoe.notificationservice.entity.Notification;
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
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getUserId(),
                notification.getOrderId(),
                notification.getTitleZh(),
                notification.getTitleEn(),
                notification.getContentZh(),
                notification.getContentEn(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }
}