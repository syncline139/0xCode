package com.example.authservice.exception.auth;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException() {
        super("Данный email занят! Попробуйте другой.");
    }
}
