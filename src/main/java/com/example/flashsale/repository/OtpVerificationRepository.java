package com.example.flashsale.repository;

import com.example.flashsale.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findByContactInfoAndOtpCodeAndIsUsedFalseAndExpiresAtAfter(
            String contactInfo,
            String otpCode,
            LocalDateTime now
    );
}
