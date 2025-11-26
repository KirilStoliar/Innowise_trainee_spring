package com.stoliar.service;

import com.stoliar.dto.UserCreateRequest;
import com.stoliar.dto.UserResponse;
import com.stoliar.exception.UserServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceClient {

    private final RestTemplate restTemplate;

    @Value("${user.service.url:http://user-service:8080}")
    private String userServiceUrl;

    public UserResponse createUser(UserCreateRequest request, String adminToken) {
        String url = userServiceUrl + "/api/v1/users";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + adminToken); // Передаем токен администратора

        // Создаем корректный JSON для user-service
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", request.getName());
        requestBody.put("surname", request.getSurname());
        requestBody.put("email", request.getEmail());
        requestBody.put("birthDate", request.getBirthDate().toString());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            log.info("Calling user-service to create user: {}", request.getEmail());

            ResponseEntity<UserResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    UserResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully created user in user-service: {}", request.getEmail());
                return response.getBody();
            } else {
                log.error("Failed to create user in user-service. Status: {}", response.getStatusCode());
                throw new UserServiceException(
                        "Failed to create user in user-service. Status: " + response.getStatusCode(),
                        HttpStatus.valueOf(response.getStatusCode().value())
                );
            }
        } catch (Exception e) {
            log.error("Error calling user-service to create user. URL: {}, Error: {}", url, e.getMessage(), e);
            throw new UserServiceException(
                    "User service unavailable: " + e.getMessage(),
                    HttpStatus.SERVICE_UNAVAILABLE
            );
        }
    }
}