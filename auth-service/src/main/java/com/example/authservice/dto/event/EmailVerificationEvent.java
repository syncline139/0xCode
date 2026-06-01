package com.example.authservice.dto.event;

public record EmailVerificationEvent(String email, String code) {
}
