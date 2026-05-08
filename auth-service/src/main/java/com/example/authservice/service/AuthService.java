package com.example.authservice.service;

import com.example.authservice.dto.request.UserRequest;
import com.example.authservice.dto.response.JwtResponse;

public interface AuthService {

    String signUp(UserRequest userDto);

    JwtResponse signIn(UserRequest userDto);
}
