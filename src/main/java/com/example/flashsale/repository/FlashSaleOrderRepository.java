package com.example.flashsale.repository;

import com.example.flashsale.entity.FlashSaleOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface FlashSaleOrderRepository extends JpaRepository<FlashSaleOrder, Long> {
    @Query("SELECT COUNT(f) > 0 FROM FlashSaleOrder f " +
            "WHERE f.userId = :userId " +
            "AND f.orderDate = :orderDate " +
            "AND f.status = 'COMPLETED'")
    boolean existsByUserIdAndOrderDate(
            @Param("userId") Long userId,
            @Param("orderDate") LocalDate orderDate
    );
}
