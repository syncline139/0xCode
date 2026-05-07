package com.example.emailsender.consumer.dto.event;

public record EmailSendEvent(String email, String code) {
}
