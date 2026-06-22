package com.example.emailsender.consumer.dto.event;

public record EmailVerificationEvent(String email, String code) {
}
