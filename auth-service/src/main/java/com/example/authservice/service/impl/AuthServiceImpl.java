package com.example.authservice.service.impl;

import com.example.authservice.constant.Role;
import com.example.authservice.dto.event.EmailSendEvent;
import com.example.authservice.dto.request.UserRequest;
import com.example.authservice.entity.EmailVerificationCode;
import com.example.authservice.entity.User;
import com.example.authservice.exception.auth.EmailAlreadyExistsException;
import com.example.authservice.mapper.UserMapper;
import com.example.authservice.producer.EmailSendProducer;
import com.example.authservice.repository.EmailVerificationCodeRepository;
import com.example.authservice.repository.UserRepository;
import com.example.authservice.security.JwtTokenProvider;
import com.example.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder encoder;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final EmailVerificationCodeRepository emailVerificationCodeRepository;
    private final EmailSendProducer emailSendProducer;

    @Override
    @Transactional
    public String signUp(UserRequest userDto) {
        if (userRepository.existsByEmail(userDto.email())) {
            throw new EmailAlreadyExistsException();
        }

        User user = userMapper.toEntity(userDto);

        String hashPassword = encoder.encode(userDto.password());
        user.setPassword(hashPassword);
        user.setRole(Role.USER);

        userRepository.save(user);

        String code = generatedCode();

        emailVerificationCodeRepository.save(new EmailVerificationCode(
                code,
                user,
                Instant.now().plus(Duration.ofMinutes(15))
        ));

        tokenProvider.createRefreshToken(user);

        emailSendProducer.sendEmail(new EmailSendEvent(userDto.email(), code));

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
