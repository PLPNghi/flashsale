package com.example.flashsale.service.impl;

import com.example.flashsale.dto.ApiResponse;
import com.example.flashsale.dto.AuthRequest;
import com.example.flashsale.dto.AuthResponse;
import com.example.flashsale.dto.OtpVerificationRequest;
import com.example.flashsale.entity.OtpVerification;
import com.example.flashsale.entity.User;
import com.example.flashsale.exception.BusinessException;
import com.example.flashsale.repository.OtpVerificationRepository;
import com.example.flashsale.repository.UserRepository;
import com.example.flashsale.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private OtpVerificationRepository otpRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "otpExpirationMinutes", 5);
        ReflectionTestUtils.setField(authService, "otpLength", 6);

        testUser = User.builder()
                .id(1L).email("test@example.com")
                .passwordHash("hashedPassword")
                .balance(new BigDecimal("10000000"))
                .emailVerified(true).build();
    }

    @Test
    void register_WithEmail_Success() {
        AuthRequest request = new AuthRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        ApiResponse<String> response = authService.register(request);

        assertTrue(response.getSuccess());
        verify(otpRepository).save(any(OtpVerification.class));

        ArgumentCaptor<OtpVerification> captor = ArgumentCaptor.forClass(OtpVerification.class);
        verify(otpRepository).save(captor.capture());
        assertEquals(6, captor.getValue().getOtpCode().length());
    }

    @Test
    void register_EmailExists_ThrowsException() {
        AuthRequest request = new AuthRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(BusinessException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void verifyOtp_Success() {
        OtpVerificationRequest request = new OtpVerificationRequest();
        request.setEmail("test@example.com");
        request.setOtpCode("123456");
        OtpVerification otp = OtpVerification.builder()
                .userId(1L).otpCode("123456")
                .contactInfo("test@example.com")
                .verificationType(OtpVerification.VerificationType.EMAIL)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .isUsed(false).build();

        when(otpRepository.findByContactInfoAndOtpCodeAndIsUsedFalseAndExpiresAtAfter(eq("test@example.com"), eq("123456"), any()))
                .thenReturn(Optional.of(otp));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        ApiResponse<String> response = authService.verifyOtp(request);

        assertTrue(response.getSuccess());
        assertTrue(otp.getIsUsed());
        assertTrue(testUser.getEmailVerified());
    }

    @Test
    void verifyOtp_InvalidOtp_ThrowsException() {
        OtpVerificationRequest request = new OtpVerificationRequest();
        request.setEmail("test@example.com");
        request.setOtpCode("999999");

        when(otpRepository.findByContactInfoAndOtpCodeAndIsUsedFalseAndExpiresAtAfter(
                anyString(),
                anyString(),
                any())).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> authService.verifyOtp(request));
    }

    @Test
    void login_Success() {
        AuthRequest request = new AuthRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");

        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(jwtUtil.generateToken("test@example.com")).thenReturn("jwt-token");
        when(jwtUtil.getExpirationTime()).thenReturn(3600000L);

        AuthResponse response = authService.login(request);

        assertEquals("jwt-token", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(3600L, response.getExpiresIn());
        assertNotNull(response.getUserInfo());
    }

    @Test
    void login_UserNotFound_ThrowsException() {
        AuthRequest request = new AuthRequest();
        request.setEmail("notfound@example.com");
        request.setPassword("password");

        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(userRepository.findByPhone(any())).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> authService.login(request));
    }
}