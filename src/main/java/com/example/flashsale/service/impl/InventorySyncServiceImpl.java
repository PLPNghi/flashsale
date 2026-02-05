package com.example.flashsale.service.impl;

import com.example.flashsale.entity.FlashSaleOrder;
import com.example.flashsale.entity.InventorySyncLog;
import com.example.flashsale.entity.Product;
import com.example.flashsale.repository.FlashSaleOrderRepository;
import com.example.flashsale.repository.InventorySyncLogRepository;
import com.example.flashsale.repository.ProductRepository;
import com.example.flashsale.service.InventorySyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventorySyncServiceImpl implements InventorySyncService {
    private final InventorySyncLogRepository syncLogRepository;
    private final ProductRepository productRepository;
    private final FlashSaleOrderRepository flashSaleOrderRepository;

    private static final String SYNC_TYPE_FLASH_SALE = "FLASH_SALE_ORDER";

    /**
     * Synchronizes inventory for a flash sale order with idempotent processing.
     * @param orderId the ID of the flash sale order
     * @param productId the ID of the product purchased
     * @throws RuntimeException if product or order is not found
     */
    @Override
    @Transactional
    public void syncInventoryForOrder(Long orderId, Long productId) {
        String referenceId = "ORDER_" + orderId;

        // Check if already synced (idempotent check)
        if (syncLogRepository.existsBySyncTypeAndReferenceId(SYNC_TYPE_FLASH_SALE, referenceId)) {
            log.info("Inventory already synced for order: {}", orderId);
            return;
        }

        // Get product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        // Get order to verify
        FlashSaleOrder order = flashSaleOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        if (order.getStatus() != FlashSaleOrder.OrderStatus.COMPLETED) {
            log.warn("Order is not completed, skip sync: {}", orderId);
            return;
        }

        int stockBefore = product.getStockQuantity() + 1; // +1 because already decreased
        int stockAfter = product.getStockQuantity();
        int quantityChange = -1; // Negative for decrease

        // Create sync log
        InventorySyncLog syncLog = InventorySyncLog.builder()
                .productId(productId)
                .quantityChange(quantityChange)
                .stockBefore(stockBefore)
                .stockAfter(stockAfter)
                .syncType(SYNC_TYPE_FLASH_SALE)
                .referenceId(referenceId)
                .build();
        syncLogRepository.save(syncLog);
        log.info("Inventory synced: productId={}, orderId={}, stockBefore={}, stockAfter={}", productId, orderId, stockBefore, stockAfter);
    }
}
