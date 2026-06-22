package com.example.authservice.repository;

import com.example.authservice.entity.EmailVerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EmailVerificationCodeRepository extends JpaRepository<EmailVerificationCode, UUID> {
    List<EmailVerificationCode> findAllByUserId(UUID userid);

}
