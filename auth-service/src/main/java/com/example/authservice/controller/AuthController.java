package com.example.authservice.controller;

import com.example.authservice.dto.request.UserRequest;
import com.example.authservice.dto.response.JwtResponse;
import com.example.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;


    @PostMapping("/sign-up")
    public ResponseEntity<String> signUp(@RequestBody @Valid UserRequest userDto) {

        authService.signUp(userDto);

        return ResponseEntity.ok("Код отправлен на email");
    }

    @PostMapping("/sign-in")
    public ResponseEntity<JwtResponse> signIn(@RequestBody @Valid UserRequest userDto) {

        JwtResponse jwtResponse = authService.signIn(userDto);
        return ResponseEntity.status(HttpStatus.OK).body(jwtResponse);
    }

    //TODO: ендпоинт для подтвержения кода
}
