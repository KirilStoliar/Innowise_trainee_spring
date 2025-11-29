package com.stoliar.service;


import com.stoliar.dto.LoginRequest;
import com.stoliar.dto.TokenResponse;
import com.stoliar.dto.TokenValidationResponse;
import com.stoliar.dto.UserCredentialsRequest;
import com.stoliar.entity.UserCredentials;

public interface AuthService {
    UserCredentials saveUserCredentials(UserCredentialsRequest request, String adminToken);
    TokenResponse login(LoginRequest request);
    TokenResponse refreshToken(String refreshToken);
    TokenValidationResponse validateToken(String token);
}