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
import com.example.flashsale.service.FlashSaleService;
import com.example.flashsale.service.InventorySyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlashSaleServiceImpl implements FlashSaleService {
    private final FlashSaleConfigRepository flashSaleConfigRepository;
    private final FlashSaleOrderRepository flashSaleOrderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CustomUserDetailsService userDetailsService;
    private final InventorySyncService inventorySyncService;

    /**
     * Retrieves all flash sale products currently active at the present time.
     * @return list of active flash sale products with complete information
     * or empty list if no flash sales are currently active
     */
    @Override
    public List<FlashSaleProductResponse> getCurrentFlashSaleProducts() {
        LocalDate today = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        List<FlashSaleConfig> activeFlashSales = flashSaleConfigRepository.findActiveFlashSales(today, currentTime);

        return activeFlashSales.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    /**
     * Processes a flash sale product purchase with strict concurrency control.
     * @param request the purchase request containing product ID
     * @return PurchaseResponse containing order details and updated balance
     * @throws BusinessException if no active flash sale exists for the product
     * @throws BusinessException if flash sale is sold out
     * @throws BusinessException if user already purchased a flash sale product today
     * @throws BusinessException if product is out of stock
     * @throws BusinessException if user has insufficient balance
     */
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public PurchaseResponse purchaseFlashSaleProduct(PurchaseRequest request) {
        // Get current user
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userDetailsService.getUserByUsername(username);

        // Check if user already purchased today
        LocalDate today = LocalDate.now();
        boolean alreadyPurchasedToday = flashSaleOrderRepository.existsByUserIdAndOrderDate(user.getId(), today);
        if (alreadyPurchasedToday) {
            throw new BusinessException("You can only purchase one flash sale product per day");
        }

        // Find active flash sale for this product
        LocalTime currentTime = LocalTime.now();
        FlashSaleConfig flashSale = flashSaleConfigRepository.findActiveFlashSaleForProduct(request.getProductId(), today, currentTime)
                .orElseThrow(() -> new BusinessException("No active flash sale for this product"));

        // Lock flash sale config with pessimistic write lock
        FlashSaleConfig lockedFlashSale = flashSaleConfigRepository.findByIdWithLock(flashSale.getId())
                .orElseThrow(() -> new BusinessException("Flash sale not found"));

        // Check if still has stock
        if (!lockedFlashSale.hasStock()) {
            throw new BusinessException("Flash sale sold out");
        }

        // Lock product for inventory update
        Product product = productRepository.findByIdWithLock(request.getProductId())
                .orElseThrow(() -> new BusinessException("Product not found"));

        // Check product stock
        if (product.getStockQuantity() <= 0) {
            throw new BusinessException("Product out of stock");
        }

        // Check user balance
        if (user.getBalance().compareTo(lockedFlashSale.getFlashPrice()) < 0) {
            throw new BusinessException("Insufficient balance");
        }

        // Deduct user balance
        user.setBalance(user.getBalance().subtract(lockedFlashSale.getFlashPrice()));
        userRepository.save(user);

        // Increment sold quantity
        lockedFlashSale.incrementSoldQuantity();
        flashSaleConfigRepository.save(lockedFlashSale);

        // Decrease product stock
        product.setStockQuantity(product.getStockQuantity() - 1);
        productRepository.save(product);

        // Create order
        FlashSaleOrder order = FlashSaleOrder.builder()
                .userId(user.getId())
                .productId(product.getId())
                .flashSaleConfigId(lockedFlashSale.getId())
                .amount(lockedFlashSale.getFlashPrice())
                .status(FlashSaleOrder.OrderStatus.COMPLETED)
                .orderedAt(LocalDateTime.now())
                .build();
        order = flashSaleOrderRepository.save(order);

        // Sync inventory (idempotent)
        inventorySyncService.syncInventoryForOrder(order.getId(), product.getId());
        log.info("Flash sale order created: orderId={}, userId={}, productId={}, amount={}",
                order.getId(), user.getId(), product.getId(), order.getAmount());

        return PurchaseResponse.builder()
                .orderId(order.getId())
                .productId(product.getId())
                .productName(product.getName())
                .amount(order.getAmount())
                .remainingBalance(user.getBalance())
                .status(order.getStatus().toString())
                .orderedAt(order.getOrderedAt())
                .message("Purchase successful!")
                .build();
    }

    private FlashSaleProductResponse mapToResponse(FlashSaleConfig config) {
        Product product = config.getProduct();
        BigDecimal discount = product.getRegularPrice()
                .subtract(config.getFlashPrice())
                .divide(product.getRegularPrice(), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
        LocalDateTime endDateTime = LocalDateTime.of(config.getSaleDate(), config.getEndTime());
        long remainingSeconds = Duration.between(LocalDateTime.now(), endDateTime).getSeconds();

        return FlashSaleProductResponse.builder()
                .flashSaleId(config.getId())
                .productId(product.getId())
                .productName(product.getName())
                .description(product.getDescription())
                .regularPrice(product.getRegularPrice())
                .flashPrice(config.getFlashPrice())
                .discountPercentage(discount.setScale(2, RoundingMode.HALF_UP))
                .availableQuantity(config.getFlashQuantity() - config.getSoldQuantity())
                .totalQuantity(config.getFlashQuantity())
                .startTime(config.getStartTime())
                .endTime(config.getEndTime())
                .remainingSeconds(Math.max(0, remainingSeconds))
                .build();
    }
}
