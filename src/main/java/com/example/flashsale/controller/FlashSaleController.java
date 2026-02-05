package com.example.flashsale.controller;

import com.example.flashsale.dto.ApiResponse;
import com.example.flashsale.dto.FlashSaleProductResponse;
import com.example.flashsale.dto.PurchaseRequest;
import com.example.flashsale.dto.PurchaseResponse;
import com.example.flashsale.service.FlashSaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/flash-sale")
@RequiredArgsConstructor
public class FlashSaleController {
    private final FlashSaleService flashSaleService;

    @GetMapping("/products/current")
    public ResponseEntity<ApiResponse<List<FlashSaleProductResponse>>> getCurrentFlashSaleProducts() {
        List<FlashSaleProductResponse> products = flashSaleService.getCurrentFlashSaleProducts();
        return ResponseEntity.ok(ApiResponse.success("Current flash sale products retrieved successfully", products));
    }

    @PostMapping("/purchase")
    public ResponseEntity<ApiResponse<PurchaseResponse>> purchaseProduct(@Valid @RequestBody PurchaseRequest request) {
        PurchaseResponse response = flashSaleService.purchaseFlashSaleProduct(request);
        return ResponseEntity.ok(ApiResponse.success("Product purchased successfully", response));
    }
}
