package com.clayjoe.notificationservice.repository;

import com.clayjoe.notificationservice.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserIdOrderByCreatedAtDescIdDesc(
            Long userId,
            Pageable pageable
    );

    long countByUserIdAndReadAtIsNull(Long userId);

    Optional<Notification> findByIdAndUserId(
            Long id,
            Long userId
    );

    Optional<Notification> findByEventIdAndUserId(
            String eventId,
            Long userId
    );
}