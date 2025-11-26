package com.stoliar.service;

import com.stoliar.dto.UserCreateDTO;
import com.stoliar.dto.UserDTO;
import com.stoliar.entity.User;
import com.stoliar.integration.AbstractIntegrationTest;
import com.stoliar.integration.TestIntegrationSecurityConfig;
import com.stoliar.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("integration-test")
@Transactional
@Import(TestIntegrationSecurityConfig.class)
class UserServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testCreateAndRetrieveUser_Integration() {
        // Given
        UserCreateDTO createDTO = new UserCreateDTO();
        createDTO.setName("Integration");
        createDTO.setSurname("Test");
        createDTO.setBirthDate(LocalDate.of(1990, 1, 1));
        createDTO.setEmail("integration@test.com");

        // When
        UserDTO createdUser = userService.createUser(createDTO);

        // Then
        assertNotNull(createdUser.getId());
        assertEquals("Integration", createdUser.getName());
        assertEquals("integration@test.com", createdUser.getEmail());
        assertTrue(createdUser.getActive());

        // When - дополнительная проверка получения пользователя
        UserDTO retrievedUser = userService.getUserById(createdUser.getId());

        // Then
        assertEquals(createdUser.getId(), retrievedUser.getId());
        assertEquals(createdUser.getName(), retrievedUser.getName());
    }

    @Test
    void testUpdateUser_Integration() {
        // Given
        User user = new User();
        user.setName("Old");
        user.setSurname("Name");
        user.setBirthDate(LocalDate.of(1990, 1, 1));
        user.setEmail("old@test.com");
        user.setActive(true);
        User savedUser = userRepository.save(user);

        UserDTO updateDTO = new UserDTO();
        updateDTO.setName("New");
        updateDTO.setSurname("Name");
        updateDTO.setBirthDate(LocalDate.of(1995, 5, 5));
        updateDTO.setEmail("new@test.com");

        // When
        UserDTO updatedUser = userService.updateUser(savedUser.getId(), updateDTO);

        // Then
        assertEquals("New", updatedUser.getName());
        assertEquals("new@test.com", updatedUser.getEmail());
    }
}