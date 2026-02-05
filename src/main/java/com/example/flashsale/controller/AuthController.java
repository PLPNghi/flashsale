package com.example.flashsale.controller;

import com.example.flashsale.dto.ApiResponse;
import com.example.flashsale.dto.AuthRequest;
import com.example.flashsale.dto.AuthResponse;
import com.example.flashsale.dto.OtpVerificationRequest;
import com.example.flashsale.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody AuthRequest request) {
        ApiResponse<String> result = authService.register(request);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .success(result.getSuccess())
                .message(result.getMessage())
                .data(result.getData())
                .timestamp(result.getTimestamp())
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<String>> verifyOtp(@Valid @RequestBody OtpVerificationRequest request) {
        ApiResponse<String> result = authService.verifyOtp(request);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .success(result.getSuccess())
                .message(result.getMessage())
                .data(result.getData())
                .timestamp(result.getTimestamp())
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        return ResponseEntity.ok(ApiResponse.success("Logout successful.", null));
    }
}
