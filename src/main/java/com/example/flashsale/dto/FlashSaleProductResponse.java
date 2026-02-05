package com.example.flashsale.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashSaleProductResponse {
    private Long flashSaleId;
    private Long productId;
    private String productName;
    private String description;
    private BigDecimal regularPrice;
    private BigDecimal flashPrice;
    private BigDecimal discountPercentage;
    private Integer availableQuantity;
    private Integer totalQuantity;
    private LocalTime startTime;
    private LocalTime endTime;
    private Long remainingSeconds;
}
