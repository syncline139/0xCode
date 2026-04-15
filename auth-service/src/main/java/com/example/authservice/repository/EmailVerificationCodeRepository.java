package com.example.authservice.repository;

import com.example.authservice.entity.EmailVerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EmailVerificationCodeRepository extends JpaRepository<EmailVerificationCode, UUID> {
}
