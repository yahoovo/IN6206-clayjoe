package com.clayjoe.notificationservice.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
        name = "notifications",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_notification_event_user",
                columnNames = {"event_id", "user_id"}
        ),
        indexes = @Index(
                name = "idx_notification_user_created",
                columnList = "user_id, created_at"
        )
)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, length = 36)
    private String eventId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "order_id")
    private Long orderId;

    @Column(nullable = false, length = 200)
    private String titleZh;

    @Column(nullable = false, length = 200)
    private String titleEn;

    @Column(nullable = false, length = 1000)
    private String contentZh;

    @Column(nullable = false, length = 1000)
    private String contentEn;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "read_at")
    private Instant readAt;

    protected Notification() {
        // JPA 加载数据库记录时需要无参构造器
    }

    public Notification(
            String eventId,
            Long userId,
            Long orderId,
            String titleZh,
            String titleEn,
            String contentZh,
            String contentEn
    ) {
        this.eventId = eventId;
        this.userId = userId;
        this.orderId = orderId;
        this.titleZh = titleZh;
        this.titleEn = titleEn;
        this.contentZh = contentZh;
        this.contentEn = contentEn;
        this.createdAt = Instant.now();
    }

    public void markRead() {
        if (readAt == null) {
            readAt = Instant.now();
        }
    }

    public Long getId() { return id; }
    public String getEventId() { return eventId; }
    public Long getUserId() { return userId; }
    public Long getOrderId() { return orderId; }
    public String getTitleZh() { return titleZh; }
    public String getTitleEn() { return titleEn; }
    public String getContentZh() { return contentZh; }
    public String getContentEn() { return contentEn; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getReadAt() { return readAt; }
}