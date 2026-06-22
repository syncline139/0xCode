package com.example.authservice.controller;

import com.example.authservice.dto.request.UserRequest;
import com.example.authservice.dto.request.VerifyRequest;
import com.example.authservice.dto.response.AuthTokens;
import com.example.authservice.security.CustomUserDetails;
import com.example.authservice.service.AuthenticationService;
import com.example.authservice.service.EmailVerificationService;
import com.example.authservice.service.RefreshTokenService;
import com.example.authservice.service.RegistrationService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";
    private static final String REFRESH_TOKEN_COOKIE_PATH = "/api/auth";
    private static final String LEGACY_REFRESH_TOKEN_COOKIE_PATH = "/api/auth/refresh";

    private final RegistrationService registrationService;
    private final AuthenticationService authenticationService;
    private final EmailVerificationService emailVerificationService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/sign-up")
    public ResponseEntity<String> signUp(@RequestBody @Valid UserRequest userDto) {

        registrationService.signUp(userDto);

        return ResponseEntity.ok("Код отправлен на email");
    }

    @PostMapping("/sign-in")
    public ResponseEntity<String> signIn(@RequestBody @Valid UserRequest userDto,
                                         HttpServletResponse response) {

        AuthTokens authTokens = authenticationService.signIn(userDto);
        addRefreshTokenInCookie(response, authTokens.refreshToken());

        return ResponseEntity.status(HttpStatus.OK).body(authTokens.accessToken());
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyAcc(@RequestBody @Valid VerifyRequest verifyRequest) {

        emailVerificationService.verifyAcc(verifyRequest);

        return ResponseEntity.ok("Аккаунт успешно подтвержен");
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> newAccessToken(@CookieValue(name = "refreshToken") String refreshToken,
                                                 HttpServletResponse response) {

        AuthTokens authTokens = refreshTokenService.newAccessToken(refreshToken);
        addRefreshTokenInCookie(response, authTokens.refreshToken());

        return ResponseEntity.ok(authTokens.accessToken());
    }

    @PostMapping("/refreshVerifyCode")
    public ResponseEntity<HttpStatus> refreshVerifyCode(@RequestBody @Valid UserRequest userDto) {

        emailVerificationService.refreshVerifyCode(userDto);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<HttpStatus> logout(@CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
                                             @AuthenticationPrincipal CustomUserDetails principal,
                                             HttpServletResponse response) {

        refreshTokenService.logout(principal.getUser().getId(), refreshToken);
        cleanRefreshToken(response);
        return ResponseEntity.ok().build();
    }

    private void addRefreshTokenInCookie(HttpServletResponse response, String newRefreshToken) {
        cleanRefreshTokenAtPath(response, LEGACY_REFRESH_TOKEN_COOKIE_PATH);

        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, newRefreshToken)
                .httpOnly(true)
                .secure(false)
                .path(REFRESH_TOKEN_COOKIE_PATH)
                .maxAge(30 * 24 * 60 * 60)
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void cleanRefreshToken(HttpServletResponse response) {
        cleanRefreshTokenAtPath(response, REFRESH_TOKEN_COOKIE_PATH);
        cleanRefreshTokenAtPath(response, LEGACY_REFRESH_TOKEN_COOKIE_PATH);
    }

    private void cleanRefreshTokenAtPath(HttpServletResponse response, String path) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(false)
                .path(path)
                .maxAge(0)
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
