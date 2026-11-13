package com.clayjoe.backend.controller;

import com.clayjoe.backend.dto.NotificationResponse;
import com.clayjoe.backend.service.NotificationQueryService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationQueryService service;

    public NotificationController(NotificationQueryService service) {
        this.service = service;
    }

    @GetMapping
    public List<NotificationResponse> list(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page
    ) {
        return service.listForUser(authentication.getName(), page);
    }
}