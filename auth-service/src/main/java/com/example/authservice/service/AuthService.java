package com.example.authservice.service;

import com.example.authservice.dto.request.UserRequest;
import com.example.authservice.dto.request.VerifyRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.userdetails.UserDetails;

public interface AuthService {

    String signUp(UserRequest userDto);

    String signIn(UserRequest userDto, HttpServletResponse response);

    void verifyAcc(VerifyRequest verifyRequest);

    String newAccessToken(String refreshToken, UserDetails userDetails);

    void refreshVerifyCode(UserRequest userDto);
}
