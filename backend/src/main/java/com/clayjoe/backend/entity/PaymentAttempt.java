package com.clayjoe.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "payment_attempts",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_payment_order_request",
                columnNames = {"order_id", "request_key"}
        )
)
public class PaymentAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private ShopOrder order;

    @Column(name = "request_key", nullable = false, length = 36)
    private String requestKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false, updatable = false)
    private boolean simulated;

    protected PaymentAttempt() {
    }

    public PaymentAttempt(
            ShopOrder order,
            String requestKey,
            PaymentMethod paymentMethod,
            PaymentStatus status) {

        this.order = order;
        this.requestKey = requestKey;
        this.paymentMethod = paymentMethod;
        this.status = status;

        // 金额和币种取自服务器上的订单。
        this.amount = order.getTotalAmount();
        this.currency = order.getCurrency();

        this.createdAt = Instant.now();
        this.simulated = true;
    }

    public Long getId() {
        return id;
    }

    public ShopOrder getOrder() {
        return order;
    }

    public String getRequestKey() {
        return requestKey;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isSimulated() {
        return simulated;
    }
}