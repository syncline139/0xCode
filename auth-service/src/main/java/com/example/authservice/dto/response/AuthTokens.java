package com.example.authservice.dto.response;

public record AuthTokens(
        String accessToken,
        String refreshToken
) {
}
