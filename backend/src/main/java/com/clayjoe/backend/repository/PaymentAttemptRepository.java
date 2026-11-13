package com.clayjoe.backend.repository;

import com.clayjoe.backend.entity.PaymentAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentAttemptRepository
        extends JpaRepository<PaymentAttempt, Long> {

    Optional<PaymentAttempt> findByOrder_IdAndRequestKey(
            Long orderId,
            String requestKey
    );

    List<PaymentAttempt> findByOrder_IdOrderByCreatedAtDescIdDesc(
            Long orderId
    );
}