package com.stoliar.user_service.service;

import com.stoliar.user_service.dto.UserCreateDTO;
import com.stoliar.user_service.dto.UserDTO;
import com.stoliar.user_service.entity.User;
import com.stoliar.user_service.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testCreateAndRetrieveUser_Integration() {

        UserCreateDTO createDTO = new UserCreateDTO();
        createDTO.setName("Integration");
        createDTO.setSurname("Test");
        createDTO.setBirthDate(LocalDate.of(1990, 1, 1));
        createDTO.setEmail("integration@test.com");

        UserDTO createdUser = userService.createUser(createDTO);

        assertNotNull(createdUser.getId());
        assertEquals("Integration", createdUser.getName());
        assertEquals("integration@test.com", createdUser.getEmail());
        assertTrue(createdUser.getActive());

        UserDTO retrievedUser = userService.getUserById(createdUser.getId());
        assertEquals(createdUser.getId(), retrievedUser.getId());
        assertEquals(createdUser.getName(), retrievedUser.getName());
    }

    @Test
    void testUpdateUser_Integration() {

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

        UserDTO updatedUser = userService.updateUser(savedUser.getId(), updateDTO);

        assertEquals("New", updatedUser.getName());
        assertEquals("new@test.com", updatedUser.getEmail());
    }
}