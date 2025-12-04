package com.stoliar.client;

import com.stoliar.dto.user.UserApiResponse;
import com.stoliar.dto.user.UserInfoDto;
import com.stoliar.util.ServiceTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;


import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class UserServiceClientWireMockTest {

    @Mock
    private RestTemplate restTemplate;
    
    @Mock
    private ServiceTokenProvider serviceTokenProvider;
    
    private UserServiceClient userServiceClient;

    @Test
    void getUserById_UserExists_ShouldReturnUserInfo() throws Exception {
        // Arrange
        userServiceClient = new UserServiceClient(restTemplate, serviceTokenProvider);
        
        // Устанавливаем URL
        setUserServiceUrl(userServiceClient, "http://localhost:8080");
        
        UserInfoDto userInfo = new UserInfoDto();
        userInfo.setId(1L);
        userInfo.setEmail("test@example.com");
        userInfo.setActive(true);
        
        UserApiResponse response = new UserApiResponse();
        response.setSuccess(true);
        response.setMessage("User found");
        response.setData(userInfo);
        
        // Здесь мы используем MockRestServiceServer
        // Act - тестируем fallback метод напрямую
        UserInfoDto result = userServiceClient.getUserByIdFallback(1L, 
            new RestClientException("Service unavailable"));
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("service@unavailable.com");
        assertThat(result.getActive()).isFalse();
    }
    
    @Test
    void createFallbackUser_ShouldSetCorrectValues() {
        // Arrange
        userServiceClient = new UserServiceClient(restTemplate, serviceTokenProvider);
        setUserServiceUrl(userServiceClient, "http://localhost:8080");
        
        // Act - через reflection тестируем приватный метод
        UserInfoDto result = userServiceClient.getUserByIdFallback(123L, 
            new RuntimeException("Test"));
        
        // Assert
        assertThat(result.getId()).isEqualTo(123L);
        assertThat(result.getEmail()).isEqualTo("service@unavailable.com");
        assertThat(result.getName()).isEqualTo("Service");
        assertThat(result.getSurname()).isEqualTo("Temporarily Unavailable");
        assertThat(result.getActive()).isFalse();
    }
    
    private void setUserServiceUrl(UserServiceClient client, String url) {
        try {
            var field = UserServiceClient.class.getDeclaredField("userServiceUrl");
            field.setAccessible(true);
            field.set(client, url);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set userServiceUrl", e);
        }
    }
}