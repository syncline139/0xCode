package com.example.authservice.controller;

import com.example.authservice.dto.request.UserRequest;
import com.example.authservice.dto.request.VerifyRequest;
import com.example.authservice.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
    public ResponseEntity<String> signIn(@RequestBody @Valid UserRequest userDto,
                                              HttpServletResponse response) {

        String accessToken = authService.signIn(userDto,response);
        return ResponseEntity.status(HttpStatus.OK).body(accessToken);
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyAcc(@RequestBody VerifyRequest verifyRequest) {

        authService.verifyAcc(verifyRequest);
        return ResponseEntity.ok("Аккаунт успешно подтвержен");
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> newAccessToken(@CookieValue(name = "refreshToken") String refreshToken,
                                            @AuthenticationPrincipal UserDetails userDetails) {

        String accessToken = authService.newAccessToken(refreshToken, userDetails);
        return ResponseEntity.ok(accessToken);
    }

    @PostMapping("/refreshVerifyCode")
    public ResponseEntity<?> refreshVerifyCode(@RequestBody @Valid UserRequest userDto) {


        authService.refreshVerifyCode(userDto);

        return ResponseEntity.ok().build();
    }
}
