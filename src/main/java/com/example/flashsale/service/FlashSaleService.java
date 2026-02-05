package com.example.flashsale.service;

import com.example.flashsale.dto.FlashSaleProductResponse;
import com.example.flashsale.dto.PurchaseRequest;
import com.example.flashsale.dto.PurchaseResponse;

import java.util.List;

public interface FlashSaleService {
    List<FlashSaleProductResponse> getCurrentFlashSaleProducts();
    PurchaseResponse purchaseFlashSaleProduct(PurchaseRequest request);
}
