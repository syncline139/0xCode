package com.example.authservice.service;

import com.example.authservice.dto.request.UserRequest;
import com.example.authservice.dto.request.VerifyRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    String signUp(UserRequest userDto);

    String signIn(UserRequest userDto, HttpServletResponse response);

    void verifyAcc(VerifyRequest verifyRequest);

    String newAccessToken(String refreshToken);

    void refreshVerifyCode(UserRequest userDto);
}
