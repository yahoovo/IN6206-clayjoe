package com.clayjoe.notificationservice.controller;

import com.clayjoe.notificationservice.dto.CreateNotificationRequest;
import com.clayjoe.notificationservice.dto.NotificationResponse;
import com.clayjoe.notificationservice.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/internal/notifications")
public class InternalNotificationController {

    private final NotificationService service;
    private final byte[] internalKey;

    public InternalNotificationController(
            NotificationService service,
            @Value("${clayjoe.internal.api-key}") String internalKey
    ) {
        if (internalKey.isBlank() || internalKey.length() < 32) {
            throw new IllegalArgumentException(
                    "Internal API key must contain at least 32 characters"
            );
        }
        this.service = service;
        this.internalKey = internalKey.getBytes(StandardCharsets.UTF_8);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationResponse create(
            @RequestHeader(value = "X-Internal-Key", required = false)
            String suppliedKey,
            @Valid @RequestBody CreateNotificationRequest request
    ) {
        checkKey(suppliedKey);
        return service.create(request);
    }

    @GetMapping
    public List<NotificationResponse> list(
            @RequestHeader(value = "X-Internal-Key", required = false)
            String suppliedKey,
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page
    ) {
        checkKey(suppliedKey);

        if (userId <= 0 || page < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid userId or page"
            );
        }

        return service.list(userId, page);
    }

    private void checkKey(String suppliedKey) {
        if (suppliedKey == null ||
                !MessageDigest.isEqual(
                        internalKey,
                        suppliedKey.getBytes(StandardCharsets.UTF_8)
                )) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleConflict() {
        return Map.of(
                "message",
                "Notification already exists or data violates database constraints"
        );
    }
}