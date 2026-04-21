package com.example.authservice.dto.event;

public record EmailSendEvent(String email, String code) {
}
