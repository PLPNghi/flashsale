package com.example.flashsale.service.impl;

import com.example.flashsale.entity.FlashSaleOrder;
import com.example.flashsale.entity.InventorySyncLog;
import com.example.flashsale.entity.Product;
import com.example.flashsale.repository.FlashSaleOrderRepository;
import com.example.flashsale.repository.InventorySyncLogRepository;
import com.example.flashsale.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventorySyncServiceImplTest {

    @Mock
    private InventorySyncLogRepository syncLogRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private FlashSaleOrderRepository flashSaleOrderRepository;

    @InjectMocks
    private InventorySyncServiceImpl inventorySyncService;

    private Product testProduct;
    private FlashSaleOrder testOrder;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id(1L).name("iPhone 15 Pro")
                .regularPrice(new BigDecimal("30000000"))
                .stockQuantity(99).build();

        testOrder = FlashSaleOrder.builder()
                .id(1L).userId(1L).productId(1L)
                .status(FlashSaleOrder.OrderStatus.COMPLETED)
                .orderedAt(LocalDateTime.now()).build();
    }

    @Test
    void syncInventoryForOrder_Success() {
        when(syncLogRepository.existsBySyncTypeAndReferenceId(any(), any())).thenReturn(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(flashSaleOrderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        inventorySyncService.syncInventoryForOrder(1L, 1L);

        ArgumentCaptor<InventorySyncLog> captor = ArgumentCaptor.forClass(InventorySyncLog.class);
        verify(syncLogRepository).save(captor.capture());

        InventorySyncLog log = captor.getValue();
        assertEquals(1L, log.getProductId());
        assertEquals(-1, log.getQuantityChange());
        assertEquals(100, log.getStockBefore());
        assertEquals(99, log.getStockAfter());
    }

    @Test
    void syncInventoryForOrder_AlreadySynced_SkipsSync() {
        when(syncLogRepository.existsBySyncTypeAndReferenceId(any(), any())).thenReturn(true);

        inventorySyncService.syncInventoryForOrder(1L, 1L);

        verify(syncLogRepository, never()).save(any());
        verify(productRepository, never()).findById(any());
    }

    @Test
    void syncInventoryForOrder_ProductNotFound_ThrowsException() {
        when(syncLogRepository.existsBySyncTypeAndReferenceId(any(), any())).thenReturn(false);
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> inventorySyncService.syncInventoryForOrder(1L, 999L));
    }
}