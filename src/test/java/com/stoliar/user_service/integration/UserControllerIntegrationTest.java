package com.stoliar.user_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stoliar.user_service.config.TestRedisConfig;
import com.stoliar.user_service.dto.UserCreateDTO;
import com.stoliar.user_service.entity.User;
import com.stoliar.user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(TestRedisConfig.class)
class UserControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setName("Integration");
        testUser.setSurname("Test");
        testUser.setBirthDate(LocalDate.of(1990, 1, 1));
        testUser.setEmail("integration.test@example.com");
        testUser.setActive(true);
        testUser = userRepository.save(testUser);
    }

    @Test
    void createUser_ValidData_ShouldReturnCreated() throws Exception {
        UserCreateDTO createDTO = new UserCreateDTO();
        createDTO.setName("John");
        createDTO.setSurname("Doe");
        createDTO.setBirthDate(LocalDate.of(1990, 1, 1));
        createDTO.setEmail("john.doe@example.com");

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.name", is("John")))
                .andExpect(jsonPath("$.data.email", is("john.doe@example.com")));
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(testUser.getId().intValue())))
                .andExpect(jsonPath("$.data.name", is("Integration")));
    }

    @Test
    void getUserById_WhenUserNotExists_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUser_ValidData_ShouldReturnUpdatedUser() throws Exception {
        String updatedJson = """
            {
                "name": "Updated",
                "surname": "User",
                "birthDate": "1990-01-01",
                "email": "updated@example.com"
            }
            """;

        mockMvc.perform(put("/api/v1/users/{id}", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatedJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.name", is("Updated")));
    }

    @Test
    void getAllUsers_ShouldReturnPaginatedUsers() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void createUser_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        UserCreateDTO createDTO = new UserCreateDTO();
        createDTO.setName(""); // пустое имя
        createDTO.setSurname(""); // пустая фамилия
        createDTO.setBirthDate(LocalDate.now().plusDays(1)); // будущая дата рождения
        createDTO.setEmail("invalid-email"); // невалидный email

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_WithDuplicateEmail_ShouldReturnConflict() throws Exception {
        UserCreateDTO createDTO = new UserCreateDTO();
        createDTO.setName("Duplicate");
        createDTO.setSurname("User");
        createDTO.setBirthDate(LocalDate.of(1990, 1, 1));
        createDTO.setEmail("integration.test@example.com"); // дублирующий email

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateUserStatus_ShouldUpdateStatus() throws Exception {
        mockMvc.perform(patch("/api/v1/users/{id}/status", testUser.getId())
                        .param("active", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.active", is(false))) // Проверяем, что вернулся объект с обновленным статусом
                .andExpect(jsonPath("$.message", containsString("deactivated")));
    }

    @Test
    void getUsersWithFilters_ShouldReturnFilteredUsers() throws Exception {
        mockMvc.perform(get("/api/v1/users/filter")
                        .param("firstName", "Integration")
                        .param("surname", "Test")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", hasSize(1)));
    }

    @Test
    void deleteUser_ShouldDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/v1/users/{id}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", containsString("deleted")));
    }
}