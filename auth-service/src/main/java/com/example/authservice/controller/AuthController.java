package com.example.authservice.controller;

import com.example.authservice.dto.request.UserRequest;
import com.example.authservice.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<String> signIn(@RequestBody @Valid UserRequest userDto,
                                              HttpServletResponse response) {

        String accessToken = authService.signIn(userDto,response);
        return ResponseEntity.status(HttpStatus.OK).body(accessToken);
    }

    //TODO: ендпоинт для подтвержения кода
}
