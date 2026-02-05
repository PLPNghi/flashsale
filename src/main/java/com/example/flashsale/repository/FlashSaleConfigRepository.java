package com.example.flashsale.repository;

import com.example.flashsale.entity.FlashSaleConfig;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlashSaleConfigRepository extends JpaRepository<FlashSaleConfig, Long> {
    @Query("SELECT f FROM FlashSaleConfig f " +
            "WHERE f.saleDate = :saleDate " +
            "AND f.startTime <= :currentTime " +
            "AND f.endTime >= :currentTime " +
            "AND f.isActive = true " +
            "AND f.soldQuantity < f.flashQuantity")
    List<FlashSaleConfig> findActiveFlashSales(
            @Param("saleDate") LocalDate saleDate,
            @Param("currentTime") LocalTime currentTime
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM FlashSaleConfig f WHERE f.id = :id")
    Optional<FlashSaleConfig> findByIdWithLock(@Param("id") Long id);

    @Query("SELECT f FROM FlashSaleConfig f " +
            "WHERE f.productId = :productId " +
            "AND f.saleDate = :saleDate " +
            "AND f.startTime <= :currentTime " +
            "AND f.endTime >= :currentTime " +
            "AND f.isActive = true")
    Optional<FlashSaleConfig> findActiveFlashSaleForProduct(
            @Param("productId") Long productId,
            @Param("saleDate") LocalDate saleDate,
            @Param("currentTime") LocalTime currentTime
    );
}
