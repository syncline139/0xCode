package com.example.authservice.service.impl;

import com.example.authservice.constant.EventType;
import com.example.authservice.constant.Role;
import com.example.authservice.dto.request.UserRequest;
import com.example.authservice.entity.EmailVerificationCode;
import com.example.authservice.entity.Outbox;
import com.example.authservice.entity.User;
import com.example.authservice.exception.auth.EmailAlreadyExistsException;
import com.example.authservice.mapper.UserMapper;
import com.example.authservice.repository.EmailVerificationCodeRepository;
import com.example.authservice.repository.OutboxRepository;
import com.example.authservice.repository.UserRepository;
import com.example.authservice.security.JwtTokenProvider;
import com.example.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.Instant;
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


    @Override
    @Transactional
    public String signUp(UserRequest userDto) {
        log.info("Началась регистрация пользоваля: {}", userDto.email());
        if (userRepository.existsByEmail(userDto.email())) {
            log.info("email: {} уже находится в БД",userDto.email());
            throw new EmailAlreadyExistsException();
        }

        User user = userMapper.toEntity(userDto);

        String hashPassword = encoder.encode(userDto.password());
        user.setPassword(hashPassword);
        user.setRole(Role.USER);

        userRepository.save(user);
        log.info("Пользователя {} сохранен в БД", userDto.email());

        String code = generatedCode();
        log.info("Сгенерирован код для подтверрдждения почты {}",code);

        EmailVerificationCode emailVerificationCode = new EmailVerificationCode(
                code,
                user,
                Instant.now().plus(Duration.ofMinutes(15)));


        emailVerificationCodeRepository.save(emailVerificationCode);
        log.info("Код успешно сохранен в БД");

        tokenProvider.createRefreshToken(user);
        log.info("Создан рефреш токен");


        Outbox event = Outbox.builder()
                .eventType(EventType.EMAIL_SEND_CODE.name())
                .payload(objectMapper.writeValueAsString(emailVerificationCode))
                .build();

        outboxRepository.save(event);
        log.info("Сообщения успешно отправлено в таблицу outbox");
        return null;
    }

    private static String generatedCode() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 5; i++) {
            sb.append(random.nextInt(0, 9));
        }

        return sb.toString();
    }

    @Override
    public String signIn(UserRequest userDto) {
        // Попытка аутентификации пользователя
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userDto.email(),
                        userDto.password()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = tokenProvider.generateAccessToken(userDetails);

        return token;
    }
}
