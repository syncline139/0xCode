package com.example.authservice.service;

import com.example.authservice.constant.EventType;
import com.example.authservice.dto.event.EmailVerificationEvent;
import com.example.authservice.dto.request.UserRequest;
import com.example.authservice.dto.request.VerifyRequest;
import com.example.authservice.entity.EmailVerificationCode;
import com.example.authservice.entity.Outbox;
import com.example.authservice.entity.User;
import com.example.authservice.exception.auth.IncorrectPasswordException;
import com.example.authservice.exception.auth.InvalidVerificationCodeException;
import com.example.authservice.repository.EmailVerificationCodeRepository;
import com.example.authservice.repository.OutboxRepository;
import com.example.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    private final UserRepository userRepository;
    private final EmailVerificationCodeRepository emailVerificationCodeRepository;
    private final ObjectMapper objectMapper;
    private final OutboxRepository outboxRepository;
    private final PasswordEncoder passwordEncoder;

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
            throw new InvalidVerificationCodeException();
        }
    }

    @Transactional
    public void refreshVerifyCode(UserRequest userRequest) {

        User user = userRepository.findByEmail(userRequest.email())
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        if (!verify(userRequest.password(), user.getPassword())) {
            log.info("Юзер не может войти в аккаунт т.к у него неверный пароль");
            throw new IncorrectPasswordException();
        }

        sendEmailVerificationCode(user);
    }

    public void sendEmailVerificationCode(User user) {
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

    private boolean verify(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
