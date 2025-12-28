package com.stoliar.service.impl;

import com.stoliar.dto.*;
import com.stoliar.entity.Role;
import com.stoliar.entity.UserCredentials;
import com.stoliar.exception.DuplicateResourceException;
import com.stoliar.exception.InvalidCredentialsException;
import com.stoliar.exception.UserServiceException;
import com.stoliar.repository.UserCredentialsRepository;
import com.stoliar.response.ApiResponse;
import com.stoliar.service.AuthService;
import com.stoliar.service.UserServiceClient;
import com.stoliar.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserCredentialsRepository userCredentialsRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserServiceClient userServiceClient;

    @Override
    @Transactional
    public UserCredentials saveUserCredentials(UserCredentialsRequest request, String adminToken) {
        log.info("Saving user credentials for username: {}", request.getEmail());

        if (userCredentialsRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Username already exists: " + request.getEmail());
        }

        // Проверяем права на создание роли ADMIN
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isCurrentUserAdmin = false;

        if (authentication != null && authentication.isAuthenticated()) {
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                if ("ROLE_ADMIN".equals(authority.getAuthority())) {
                    isCurrentUserAdmin = true;
                    break;
                }
            }
        }

        Role requestedRole = request.getRole();

        // Только ADMIN может создавать пользователей с ролью ADMIN
        if (requestedRole == Role.ADMIN && !isCurrentUserAdmin) {
            throw new SecurityException("Only ADMIN users can create users with ADMIN role");
        }

        // Создаем credentials
        UserCredentials credentials = new UserCredentials();
        credentials.setEmail(request.getEmail());
        credentials.setPassword(passwordEncoder.encode(request.getPassword()));
        credentials.setRole(requestedRole);
        credentials.setActive(true);
        credentials.setName(request.getName());
        credentials.setSurname(request.getSurname());
        credentials.setBirthDate(request.getBirthDate());

        UserCredentials savedCredentials = userCredentialsRepository.save(credentials);

        try {
            // Создаем запись в user-service с передачей adminToken
            UserCreateRequest userCreateRequest = new UserCreateRequest();
            userCreateRequest.setEmail(request.getEmail());
            userCreateRequest.setRole(requestedRole);
            userCreateRequest.setName(request.getName());
            userCreateRequest.setSurname(request.getSurname());
            userCreateRequest.setBirthDate(request.getBirthDate());

            UserResponse userResponse = userServiceClient.createUser(userCreateRequest, adminToken);
            log.info("Successfully created user in user-service with id: {}", userResponse.getId());

        } catch (UserServiceException e) {
            log.error("Failed to create user in user-service. Rolling back auth-service creation", e);
            userCredentialsRepository.delete(savedCredentials);
            throw new RuntimeException("Failed to create user profile: " + e.getMessage());
        }

        return savedCredentials;
    }

    @Override
    @Transactional
    public TokenResponse login(LoginRequest request) {
        log.info("Login attempt for username: {}", request.getEmail());

        UserCredentials credentials = userCredentialsRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        if (!credentials.getActive()) {
            throw new InvalidCredentialsException("Account is deactivated");
        }

        if (!passwordEncoder.matches(request.getPassword(), credentials.getPassword())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(
                credentials.getEmail(),
                credentials.getRole(),
                credentials.getId()
        );

        String refreshToken = jwtTokenProvider.generateRefreshToken(
                credentials.getEmail(),
                credentials.getRole(),
                credentials.getId()
        );

        // Сохраняем refresh token в базу
        credentials.setRefreshToken(refreshToken);
        credentials.setRefreshTokenExpiry(LocalDateTime.now().plusSeconds(jwtTokenProvider.getJwtProperties().getRefreshTokenExpiration() / 1000));
        userCredentialsRepository.save(credentials);

        log.info("Successful login for username: {}", request.getEmail());

        return new TokenResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtTokenProvider.getTokenExpirationInSeconds(accessToken)
        );
    }

    @Override
    @Transactional
    public TokenResponse refreshToken(String refreshToken) {
        log.info("Refresh token request");

        // Проверяем, что токен является refresh token
        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new InvalidCredentialsException("Invalid refresh token");
        }

        // Проверяем валидность токена
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new InvalidCredentialsException("Invalid or expired refresh token");
        }

        String email = jwtTokenProvider.getUsernameFromToken(refreshToken);
        UserCredentials credentials = userCredentialsRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        if (!credentials.getActive()) {
            throw new InvalidCredentialsException("Account is deactivated");
        }

        // Проверяем, что refresh token совпадает с сохраненным
        if (!refreshToken.equals(credentials.getRefreshToken())) {
            throw new InvalidCredentialsException("Invalid refresh token");
        }

        // Проверяем срок действия refresh token
        if (credentials.getRefreshTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new InvalidCredentialsException("Refresh token expired");
        }

        // Генерируем новую пару токенов
        String newAccessToken = jwtTokenProvider.generateAccessToken(
                credentials.getEmail(),
                credentials.getRole(),
                credentials.getId()
        );

        String newRefreshToken = jwtTokenProvider.generateRefreshToken(
                credentials.getEmail(),
                credentials.getRole(),
                credentials.getId()
        );

        // Обновляем refresh token в базе
        credentials.setRefreshToken(newRefreshToken);
        credentials.setRefreshTokenExpiry(LocalDateTime.now().plusSeconds(jwtTokenProvider.getJwtProperties().getRefreshTokenExpiration() / 1000));
        userCredentialsRepository.save(credentials);

        log.info("Token refreshed for username: {}", email);

        return new TokenResponse(
                newAccessToken,
                newRefreshToken,
                "Bearer",
                jwtTokenProvider.getTokenExpirationInSeconds(newAccessToken)
        );
    }

    @Override
    public TokenValidationResponse validateToken(String token) {
        log.info("Validating token");

        if (!jwtTokenProvider.validateToken(token)) {
            return new TokenValidationResponse(false, null, null, "Invalid token");
        }

        String username = jwtTokenProvider.getEmailFromToken(token);
        Role role = jwtTokenProvider.getRoleFromToken(token);

        return new TokenValidationResponse(true, username, role, "Token is valid");
    }

    public ResponseEntity<ApiResponse<Void>> deleteUserForRollback(Long id, String serviceName) {
        log.info("Deleting user for rollback, id: {}", id);

        try {
            // 1. Удаляем пользователя в user-service
            ResponseEntity<ApiResponse<Void>> userServiceResponse = userServiceClient.deleteUserForRollback(id, serviceName);

            if (!userServiceResponse.getStatusCode().is2xxSuccessful()) {
                log.error("Failed to delete user in user-service. Status: {}", userServiceResponse.getStatusCode());
                return userServiceResponse;
            }

            // 2. Удаляем пользователя в auth-service
            if (!userCredentialsRepository.existsById(id)) {
                log.warn("User not found in auth-service, id: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("User not found in auth-service"));
            }

            userCredentialsRepository.deleteById(id);

            log.info("User deleted successfully from both services, id: {}", id);
            return ResponseEntity.ok(ApiResponse.success(null, "User deleted for rollback"));

        } catch (UserServiceException e) {
            log.error("Failed to delete user in user-service during rollback. Auth record will be preserved. Error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete user in user-service: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during rollback deletion, id: {}, error: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Unexpected error: " + e.getMessage()));
        }
    }
}