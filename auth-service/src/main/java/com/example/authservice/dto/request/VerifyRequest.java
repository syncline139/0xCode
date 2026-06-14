package com.example.authservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record VerifyRequest(
        @Email
        @NotBlank(message = "Email may not be blank")
        String email,

        @NotBlank(message = "Code may not be blank")
        String code) {
}
