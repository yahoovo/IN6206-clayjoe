package com.clayjoe.notificationservice.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;

public record CreateNotificationRequest(
        @NotNull UUID eventId,
        @NotNull @Positive Long userId,
        @Positive Long orderId,
        @NotBlank @Size(max = 200) String titleZh,
        @NotBlank @Size(max = 200) String titleEn,
        @NotBlank @Size(max = 1000) String contentZh,
        @NotBlank @Size(max = 1000) String contentEn
) {
}