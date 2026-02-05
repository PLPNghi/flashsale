package com.example.flashsale.service;

import com.example.flashsale.dto.ApiResponse;
import com.example.flashsale.dto.AuthRequest;
import com.example.flashsale.dto.AuthResponse;
import com.example.flashsale.dto.OtpVerificationRequest;

public interface AuthService {
    ApiResponse<String> register(AuthRequest request);
    ApiResponse<String> verifyOtp(OtpVerificationRequest request);
    AuthResponse login(AuthRequest request);
}
