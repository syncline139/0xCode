package com.example.authservice.service;

import com.example.authservice.dto.response.AuthTokens;
import com.example.authservice.entity.RefreshToken;
import com.example.authservice.entity.User;
import com.example.authservice.exception.auth.RefreshTokenNotFoundException;
import com.example.authservice.repository.RefreshTokenRepository;
import com.example.authservice.security.CustomUserDetails;
import com.example.authservice.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthTokens newAccessToken(String maybeRefreshToken) {
        if (!StringUtils.hasText(maybeRefreshToken)) {
            throw new IllegalArgumentException("Рефреш токен отсутствует или пустой");
        }

        RefreshToken refreshToken = refreshTokenRepository.findByToken(maybeRefreshToken)
                .orElseThrow(() -> new RefreshTokenNotFoundException("не найден токен"));

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Сроку действия токина истек");
        }

        User user = refreshToken.getUser();
        AuthTokens authTokens = issueTokens(user);

        return new AuthTokens(authTokens.accessToken(), authTokens.refreshToken());
    }


    public AuthTokens issueTokens(User user) {
        refreshTokenRepository.deleteAllByUserId(user.getId());

        CustomUserDetails details = new CustomUserDetails(user);

        return new AuthTokens(
                jwtTokenProvider.generateAccessToken(details),
                jwtTokenProvider.createRefreshToken(user)
        );
    }
}
