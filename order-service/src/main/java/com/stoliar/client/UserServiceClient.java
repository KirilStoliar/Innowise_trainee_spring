package com.stoliar.client;

import com.stoliar.dto.UserInfoDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserServiceClient {

    private final RestTemplate restTemplate;

    @Value("${user.service.url:http://user-service:8080}")
    private String userServiceUrl;

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserByIdFallback")
    @Retry(name = "userService", fallbackMethod = "getUserByIdFallback")
    public UserInfoDto getUserById(Long userId) {
        log.info("Calling User Service for userId: {}", userId);

        String url = userServiceUrl + "/api/v1/users/" + userId;

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<UserInfoDto> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                UserInfoDto.class
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody();
        } else {
            log.error("Failed to get user info. Status: {}", response.getStatusCode());
            throw new RuntimeException("Failed to get user info from user service");
        }
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserByEmailFallback")
    @Retry(name = "userService", fallbackMethod = "getUserByEmailFallback")
    public UserInfoDto getUserByEmail(String email) {
        log.info("Calling User Service for email: {}", email);

        String url = userServiceUrl + "/api/v1/users/email/" + email;

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<UserInfoDto> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                UserInfoDto.class
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody();
        } else {
            log.error("Failed to get user info by email. Status: {}", response.getStatusCode());
            throw new RuntimeException("Failed to get user info from user service");
        }
    }

    // Fallback методы
    public UserInfoDto getUserByIdFallback(Long userId, Exception e) {
        log.warn("Circuit Breaker Fallback triggered for userId: {}. Error: {}", userId, e.getMessage());
        return createFallbackUser(userId);
    }

    public UserInfoDto getUserByEmailFallback(String email, Exception e) {
        log.warn("Circuit Breaker Fallback triggered for email: {}. Error: {}", email, e.getMessage());
        return createFallbackUser(email);
    }

    private UserInfoDto createFallbackUser(Long userId) {
        UserInfoDto fallback = new UserInfoDto();
        fallback.setId(userId);
        fallback.setEmail("service@unavailable.com");
        fallback.setName("Service");
        fallback.setSurname("Temporarily Unavailable");
        fallback.setActive(false);
        return fallback;
    }

    private UserInfoDto createFallbackUser(String email) {
        UserInfoDto fallback = new UserInfoDto();
        fallback.setId(-1L);
        fallback.setEmail(email);
        fallback.setName("Service");
        fallback.setSurname("Temporarily Unavailable");
        fallback.setActive(false);
        return fallback;
    }
}