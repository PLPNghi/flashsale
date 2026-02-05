package com.example.flashsale.service;

public interface InventorySyncService {
    void syncInventoryForOrder(Long orderId, Long productId);
}
