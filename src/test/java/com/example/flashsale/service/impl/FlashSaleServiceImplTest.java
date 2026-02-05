package com.example.flashsale.service.impl;

import com.example.flashsale.dto.FlashSaleProductResponse;
import com.example.flashsale.dto.PurchaseRequest;
import com.example.flashsale.dto.PurchaseResponse;
import com.example.flashsale.entity.FlashSaleConfig;
import com.example.flashsale.entity.FlashSaleOrder;
import com.example.flashsale.entity.Product;
import com.example.flashsale.entity.User;
import com.example.flashsale.exception.BusinessException;
import com.example.flashsale.repository.FlashSaleConfigRepository;
import com.example.flashsale.repository.FlashSaleOrderRepository;
import com.example.flashsale.repository.ProductRepository;
import com.example.flashsale.repository.UserRepository;
import com.example.flashsale.service.CustomUserDetailsService;
import com.example.flashsale.service.InventorySyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlashSaleServiceImplTest {
    @Mock
    private FlashSaleConfigRepository flashSaleConfigRepository;

    @Mock
    private FlashSaleOrderRepository flashSaleOrderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private InventorySyncService inventorySyncService;

    @InjectMocks
    private FlashSaleServiceImpl flashSaleService;

    private User testUser;

    private Product testProduct;

    private FlashSaleConfig testFlashSaleConfig;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L).email("test@example.com")
                .balance(new BigDecimal("50000000")).build();

        testProduct = Product.builder()
                .id(1L).name("iPhone 15 Pro")
                .regularPrice(new BigDecimal("30000000"))
                .stockQuantity(100).build();

        testFlashSaleConfig = FlashSaleConfig.builder()
                .id(1L).product(testProduct)
                .flashPrice(new BigDecimal("25000000"))
                .flashQuantity(50).soldQuantity(10)
                .saleDate(LocalDate.now())
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(12, 0)).build();
    }

    @Test
    void getCurrentFlashSaleProducts_Success() {
        when(flashSaleConfigRepository.findActiveFlashSales(any(), any())).thenReturn(Arrays.asList(testFlashSaleConfig));

        List<FlashSaleProductResponse> responses = flashSaleService.getCurrentFlashSaleProducts();

        assertEquals(1, responses.size());
        assertEquals(40, responses.get(0).getAvailableQuantity());
    }

    @Test
    void purchaseFlashSaleProduct_Success() {
        PurchaseRequest request = new PurchaseRequest();
        request.setProductId(1L);
        setupSecurityContext("test@example.com");

        when(userDetailsService.getUserByUsername("test@example.com")).thenReturn(testUser);
        when(flashSaleOrderRepository.existsByUserIdAndOrderDate(1L, LocalDate.now())).thenReturn(false);
        when(flashSaleConfigRepository.findActiveFlashSaleForProduct(any(), any(), any())).thenReturn(Optional.of(testFlashSaleConfig));
        when(flashSaleConfigRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testFlashSaleConfig));
        when(productRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testProduct));
        when(flashSaleOrderRepository.save(any())).thenReturn(
                FlashSaleOrder.builder().id(1L).status(FlashSaleOrder.OrderStatus.COMPLETED).build());

        PurchaseResponse response = flashSaleService.purchaseFlashSaleProduct(request);

        assertEquals("Purchase successful!", response.getMessage());
        verify(inventorySyncService).syncInventoryForOrder(1L, 1L);
    }

    @Test
    void purchaseFlashSaleProduct_AlreadyPurchased() {
        PurchaseRequest request = new PurchaseRequest();
        request.setProductId(1L);
        setupSecurityContext("test@example.com");

        when(userDetailsService.getUserByUsername(any())).thenReturn(testUser);
        when(flashSaleOrderRepository.existsByUserIdAndOrderDate(1L, LocalDate.now())).thenReturn(true);

        assertThrows(BusinessException.class, () -> flashSaleService.purchaseFlashSaleProduct(request));
    }

    @Test
    void purchaseFlashSaleProduct_InsufficientBalance() {
        testUser.setBalance(new BigDecimal("100"));
        PurchaseRequest request = new PurchaseRequest();
        request.setProductId(1L);
        setupSecurityContext("test@example.com");

        when(userDetailsService.getUserByUsername(any())).thenReturn(testUser);
        when(flashSaleOrderRepository.existsByUserIdAndOrderDate(any(), any())).thenReturn(false);
        when(flashSaleConfigRepository.findActiveFlashSaleForProduct(any(), any(), any())).thenReturn(Optional.of(testFlashSaleConfig));
        when(flashSaleConfigRepository.findByIdWithLock(any())).thenReturn(Optional.of(testFlashSaleConfig));
        when(productRepository.findByIdWithLock(any())).thenReturn(Optional.of(testProduct));

        assertThrows(BusinessException.class, () -> flashSaleService.purchaseFlashSaleProduct(request));
    }

    private void setupSecurityContext(String username) {
        Authentication auth = new UsernamePasswordAuthenticationToken(username, null);
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);
    }
}