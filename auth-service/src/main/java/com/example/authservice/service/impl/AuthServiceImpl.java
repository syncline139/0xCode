package com.example.authservice.service.impl;

import com.example.authservice.constant.EventType;
import com.example.authservice.constant.Role;
import com.example.authservice.dto.event.EmailVerificationEvent;
import com.example.authservice.dto.request.UserRequest;
import com.example.authservice.dto.request.VerifyRequest;
import com.example.authservice.entity.EmailVerificationCode;
import com.example.authservice.entity.Outbox;
import com.example.authservice.entity.RefreshToken;
import com.example.authservice.entity.User;
import com.example.authservice.exception.auth.EmailAlreadyExistsException;
import com.example.authservice.exception.auth.IncorrectPasswordException;
import com.example.authservice.exception.auth.RefreshTokenNotFoundException;
import com.example.authservice.mapper.UserMapper;
import com.example.authservice.repository.EmailVerificationCodeRepository;
import com.example.authservice.repository.OutboxRepository;
import com.example.authservice.repository.RefreshTokenRepository;
import com.example.authservice.repository.UserRepository;
import com.example.authservice.security.CustomUserDetails;
import com.example.authservice.security.JwtTokenProvider;
import com.example.authservice.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder encoder;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final EmailVerificationCodeRepository emailVerificationCodeRepository;
    private final ObjectMapper objectMapper;
    private final OutboxRepository outboxRepository;
    private final RefreshTokenRepository refreshTokenRepository;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public String signUp(UserRequest userDto) {
        log.info("Началась регистрация пользоваля: {}", userDto.email());
        if (userRepository.existsByEmail(userDto.email())) {
            log.info("email: {} уже находится в БД", userDto.email());
            throw new EmailAlreadyExistsException();
        }

        User user = userMapper.toEntity(userDto);

        String hashPassword = encoder.encode(userDto.password());
        user.setPassword(hashPassword);
        user.setRole(Role.ROLE_USER);

        userRepository.save(user);
        log.info("Пользователя {} сохранен в БД", userDto.email());

        sendEmailVerificationCode(user);

        tokenProvider.createRefreshToken(user);
        log.info("Создан рефреш токен");

        return null;
    }

    private void sendEmailVerificationCode(User user) {
        String code = generatedCode();
        log.info("Сгенерирован код для подтверрдждения почты {}", code);

        EmailVerificationCode emailVerificationCode = new EmailVerificationCode(
                code,
                user,
                Instant.now().plus(Duration.ofMinutes(15)));


        emailVerificationCodeRepository.save(emailVerificationCode);
        log.info("Код успешно сохранен в БД");


        Outbox event = Outbox.builder()
                .eventType(EventType.EMAIL_SEND_CODE.name())
                .payload(objectMapper.writeValueAsString(new EmailVerificationEvent(user.getEmail(), code)))
                .build();

        outboxRepository.save(event);
        log.info("Сообщения успешно сохранено в таблице outbox");
    }

    private static String generatedCode() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 5; i++) {
            sb.append(random.nextInt(0, 10));
        }

        return sb.toString();
    }

    @Override
    @Transactional
    public String signIn(UserRequest userDto, HttpServletResponse response) {
        // Попытка аутентификации пользователя
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userDto.email(),
                        userDto.password()
                )
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        String accessToken = tokenProvider.generateAccessToken(userDetails);

        refreshTokenRepository.deleteAllByUserId(user.getId());
        String refreshToken = tokenProvider.createRefreshToken(user);

        addRefreshTokenInCookie(response, refreshToken);

        return accessToken;
    }

    @Override
    @Transactional
    public void verifyAcc(VerifyRequest verifyRequest) {

        User user = userRepository.findByEmail(verifyRequest.email())
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        if (user.isEmailVerified()) {
            throw new IllegalArgumentException("Акканут уже подтвержден");
        }


        List<EmailVerificationCode> codes = emailVerificationCodeRepository.findAllByUserId(user.getId());

        boolean flag = false;

        for (EmailVerificationCode code : codes) {
            if (code.getCode().equalsIgnoreCase(verifyRequest.code())
                    && code.getExpiresAt().isAfter(Instant.now())) {
                user.setEmailVerified(true);
                flag = true;
            }
        }
        if (!flag) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Неверный код для подтверждения акканут");
        }
    }

    @Override
    public String newAccessToken(String maybeRefreshToken, HttpServletResponse response) {
        if (!StringUtils.hasText(maybeRefreshToken)) {
            throw new IllegalArgumentException("Рефреш токен отсутствует или пустой");
        }

        RefreshToken refreshToken = refreshTokenRepository.findByToken(maybeRefreshToken)
                .orElseThrow(() -> new RefreshTokenNotFoundException("не найден токен"));

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Сроку действия токина истек");
        }

        User user = refreshToken.getUser();

        refreshTokenRepository.deleteAllByUserId(refreshToken.getUser().getId());
        String newRefreshToken = tokenProvider.createRefreshToken(user);

        addRefreshTokenInCookie(response, newRefreshToken);

        CustomUserDetails userDetails = new CustomUserDetails(user);
        return tokenProvider.generateAccessToken(userDetails);
    }

    private void addRefreshTokenInCookie(HttpServletResponse response, String newRefreshToken) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", newRefreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/api/auth/refresh")
                .maxAge(30 * 24 * 60 * 60)
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @Override
    public void refreshVerifyCode(UserRequest userRequest) {

        User user = userRepository.findByEmail(userRequest.email())
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        if (!verify(userRequest.password(), user.getPassword())) {
            log.info("Юзер не может войти в аккаунт т.к у него неверный пароль");
            throw new IncorrectPasswordException();
        }

        sendEmailVerificationCode(user);
    }

    private boolean verify(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}
