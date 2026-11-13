package com.clayjoe.backend.repository;

import com.clayjoe.backend.entity.CartItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @EntityGraph(attributePaths = "product")
    List<CartItem> findByUser_IdOrderByIdAsc(Long userId);

    Optional<CartItem> findByUser_IdAndProduct_Id(
            Long userId,
            Long productId
    );

    Optional<CartItem> findByIdAndUser_Id(
            Long id,
            Long userId
    );
}