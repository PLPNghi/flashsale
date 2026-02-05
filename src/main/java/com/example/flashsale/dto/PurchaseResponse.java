package com.example.flashsale.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseResponse {
    private Long orderId;
    private Long productId;
    private String productName;
    private BigDecimal amount;
    private BigDecimal remainingBalance;
    private String status;
    private LocalDateTime orderedAt;
    private String message;
}
