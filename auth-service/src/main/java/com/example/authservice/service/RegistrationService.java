package com.example.authservice.service;

import com.example.authservice.constant.Role;
import com.example.authservice.dto.request.UserRequest;
import com.example.authservice.entity.User;
import com.example.authservice.exception.auth.EmailAlreadyExistsException;
import com.example.authservice.mapper.UserMapper;
import com.example.authservice.repository.UserRepository;
import com.example.authservice.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder encoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailVerificationService emailVerificationService;

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

        emailVerificationService.sendEmailVerificationCode(user);

        jwtTokenProvider.createRefreshToken(user);
        log.info("Создан рефреш токен");

        return null;
    }
}
