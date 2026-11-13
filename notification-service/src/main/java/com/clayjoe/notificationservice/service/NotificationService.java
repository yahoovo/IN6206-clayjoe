package com.clayjoe.notificationservice.service;

import com.clayjoe.notificationservice.dto.CreateNotificationRequest;
import com.clayjoe.notificationservice.dto.NotificationResponse;
import com.clayjoe.notificationservice.entity.Notification;
import com.clayjoe.notificationservice.repository.NotificationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public NotificationResponse create(CreateNotificationRequest request) {
        Notification notification = new Notification(
                request.eventId().toString(),
                request.userId(),
                request.orderId(),
                request.titleZh(),
                request.titleEn(),
                request.contentZh(),
                request.contentEn()
        );

        Notification saved = repository.saveAndFlush(notification);
        return NotificationResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> list(Long userId, int page) {
        return repository
                .findByUserIdOrderByCreatedAtDescIdDesc(
                        userId,
                        PageRequest.of(page, 20)
                )
                .getContent()
                .stream()
                .map(NotificationResponse::from)
                .toList();
    }
}