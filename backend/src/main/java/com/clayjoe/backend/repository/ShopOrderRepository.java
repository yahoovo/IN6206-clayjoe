package com.clayjoe.backend.repository;

import com.clayjoe.backend.entity.ShopOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ShopOrderRepository
        extends JpaRepository<ShopOrder, Long> {

    List<ShopOrder> findByUser_IdOrderByCreatedAtDescIdDesc(
            Long userId
    );
    List<ShopOrder> findAllByOrderByCreatedAtDescIdDesc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from ShopOrder o where o.id = :id")
    Optional<ShopOrder> findOrderForUpdate(@Param("id") Long id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    select o from ShopOrder o
    where o.id = :id and o.user.id = :userId
    """)
    Optional<ShopOrder> findOwnedOrderForUpdate(
            @Param("id") Long id,
            @Param("userId") Long userId
    );
    @EntityGraph(attributePaths = "items")
    Optional<ShopOrder> findByIdAndUser_Id(
            Long id,
            Long userId
    );
    Optional<ShopOrder> findByUser_IdAndRequestKey(
            Long userId,
            String requestKey
    );
}