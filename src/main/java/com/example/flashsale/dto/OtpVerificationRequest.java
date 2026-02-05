package com.example.flashsale.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpVerificationRequest {
    private String email;
    private String phone;

    @NotBlank(message = "OTP code is required")
    private String otpCode;
}
