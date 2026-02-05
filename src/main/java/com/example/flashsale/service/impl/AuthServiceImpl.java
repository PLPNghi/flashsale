package com.example.flashsale.service.impl;

import com.example.flashsale.dto.ApiResponse;
import com.example.flashsale.dto.AuthRequest;
import com.example.flashsale.dto.AuthResponse;
import com.example.flashsale.dto.OtpVerificationRequest;
import com.example.flashsale.dto.UserInfo;
import com.example.flashsale.entity.OtpVerification;
import com.example.flashsale.entity.User;
import com.example.flashsale.exception.BusinessException;
import com.example.flashsale.repository.OtpVerificationRepository;
import com.example.flashsale.repository.UserRepository;
import com.example.flashsale.security.JwtUtil;
import com.example.flashsale.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final OtpVerificationRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Value("${otp.expiration-minutes}")
    private int otpExpirationMinutes;

    @Value("${otp.length}")
    private int otpLength;

    /**
     * Registers a new user with email or phone number.
     * @param request the registration request containing email/phone and password
     * @return ApiResponse containing success message and OTP delivery confirmation
     * @throws BusinessException if email or phone is required but not provided
     * @throws BusinessException if email already exists in the system
     * @throws BusinessException if phone number already exists in the system
     */
    @Override
    @Transactional
    public ApiResponse<String> register(AuthRequest request) {
        // Validate input
        if (request.getEmail() == null && request.getPhone() == null) {
            throw new BusinessException("Email or phone number is required");
        }

        // Check if user already exists
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already exists");
        }
        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException("Phone number already exists");
        }

        // Create user
        User user = User.builder()
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .balance(new BigDecimal("100000000")) // starting balance for demo
                .emailVerified(false)
                .phoneVerified(false)
                .build();
        user = userRepository.save(user);

        // Generate and send OTP
        String otp = generateOtp();
        String contactInfo = request.getEmail() != null ? request.getEmail() : request.getPhone();
        OtpVerification.VerificationType type = request.getEmail() != null 
                ? OtpVerification.VerificationType.EMAIL 
                : OtpVerification.VerificationType.PHONE;
        OtpVerification otpVerification = OtpVerification.builder()
                .userId(user.getId())
                .otpCode(otp)
                .verificationType(type)
                .contactInfo(contactInfo)
                .expiresAt(LocalDateTime.now().plusMinutes(otpExpirationMinutes))
                .isUsed(false)
                .build();
        otpRepository.save(otpVerification);

        // Mock send OTP
        log.info("=== OTP VERIFICATION ===");
        log.info("Contact: {}", contactInfo);
        log.info("OTP Code: {}", otp);
        log.info("Expires at: {}", otpVerification.getExpiresAt());
        log.info("========================");

        return new ApiResponse<>(
                true,
                "Registration successful. Please verify your " + type.toString().toLowerCase() + " with OTP.",
                "OTP sent to " + contactInfo,
                LocalDateTime.now(),
                null
        );
    }

    /**
     * Verifies the OTP code sent to user's email or phone number.
     * @param request the OTP verification request containing email/phone and OTP code
     * @return ApiResponse with success message
     * @throws BusinessException if OTP is invalid, expired, or already used
     * @throws BusinessException if user associated with OTP is not found
     */
    @Override
    @Transactional
    public ApiResponse<String> verifyOtp(OtpVerificationRequest request) {
        String contactInfo = request.getEmail() != null ? request.getEmail() : request.getPhone();

        OtpVerification otpVerification = otpRepository
                .findByContactInfoAndOtpCodeAndIsUsedFalseAndExpiresAtAfter(
                        contactInfo,
                        request.getOtpCode(),
                        LocalDateTime.now()
                )
                .orElseThrow(() -> new BusinessException("Invalid or expired OTP"));

        // Mark OTP as used
        otpVerification.setIsUsed(true);
        otpRepository.save(otpVerification);

        // Update user verification status
        User user = userRepository.findById(otpVerification.getUserId())
                .orElseThrow(() -> new BusinessException("User not found"));

        if (otpVerification.getVerificationType() == OtpVerification.VerificationType.EMAIL) {
            user.setEmailVerified(true);
        } else {
            user.setPhoneVerified(true);
        }

        userRepository.save(user);

        return new ApiResponse<>(
                true,
                "Verification successful",
                null,
                LocalDateTime.now(),
                null
        );
    }

    /**
     * Authenticates a user and generates a JWT access token.
     * @param request the login request containing email/phone and password
     * @return AuthResponse containing JWT token, token metadata, and user information
     * @throws BusinessException if email or phone is required but not provided
     * @throws BusinessException if user is not found in the system
     */
    @Override
    public AuthResponse login(AuthRequest request) {
        String username = request.getEmail() != null ? request.getEmail() : request.getPhone();

        if (username == null) {
            throw new BusinessException("Email or phone number is required");
        }

        // Authenticate
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, request.getPassword()));

        // Get user
        User user = userRepository.findByEmail(username)
                .or(() -> userRepository.findByPhone(username))
                .orElseThrow(() -> new BusinessException("User not found"));

        // Generate JWT token
        String token = jwtUtil.generateToken(username);

        // Build response
        UserInfo userInfo = UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .emailVerified(user.getEmailVerified())
                .phoneVerified(user.getPhoneVerified())
                .build();

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationTime() / 1000) // Convert to seconds
                .userInfo(userInfo)
                .build();
    }

    private String generateOtp() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
}
