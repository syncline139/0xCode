package com.example.authservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRequest(
        @Email
        @NotBlank(message = "Email may not be blank")
        String email,

        @NotBlank(message = "Password may not be blank")
        String password) {
}
