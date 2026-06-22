package com.example.authservice.service;

import com.example.authservice.dto.request.UserRequest;
import com.example.authservice.dto.response.AuthTokens;
import com.example.authservice.entity.User;
import com.example.authservice.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AuthTokens signIn(UserRequest userDto) {
        // Попытка аутентификации пользователя
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userDto.email(),
                        userDto.password()
                )
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();
        AuthTokens authTokens = refreshTokenService.issueTokens(user);

        return new AuthTokens(authTokens.accessToken(), authTokens.refreshToken());
    }
}
