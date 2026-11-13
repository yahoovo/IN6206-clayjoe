package com.clayjoe.notificationservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class NotificationHealthController {

    @GetMapping("/api/notifications/health")
    public Map<String, String> health() {
        return Map.of(
                "status", "ok",
                "service", "notification-service"
        );
    }
}