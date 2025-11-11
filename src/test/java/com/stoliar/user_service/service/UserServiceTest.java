package com.stoliar.user_service.service;

import com.stoliar.user_service.dto.UserCreateDTO;
import com.stoliar.user_service.dto.UserDTO;
import com.stoliar.user_service.entity.User;
import com.stoliar.user_service.mapper.UserMapper;
import com.stoliar.user_service.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void testCreateUser_ValidData_ShouldReturnUserDTO() {

        UserCreateDTO createDTO = new UserCreateDTO();
        createDTO.setName("John");
        createDTO.setSurname("Doe");
        createDTO.setBirthDate(LocalDate.of(1990, 1, 1));
        createDTO.setEmail("john.doe@example.com");

        User user = new User();
        user.setId(1L);
        user.setName("John");
        user.setSurname("Doe");

        UserDTO expectedDTO = new UserDTO();
        expectedDTO.setId(1L);
        expectedDTO.setName("John");

        when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(false);
        when(userMapper.toEntity(createDTO)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDTO(user)).thenReturn(expectedDTO);

        UserDTO result = userService.createUser(createDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John", result.getName());
        verify(userRepository).existsByEmail("john.doe@example.com");
        verify(userRepository).save(user);
    }

    @Test
    void testCreateUser_DuplicateEmail_ShouldThrowException() {

        UserCreateDTO createDTO = new UserCreateDTO();
        createDTO.setEmail("existing@example.com");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(createDTO));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testGetUserById_WhenUserExists_ShouldReturnUserDTO() {

        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setName("John");

        UserDTO expectedDTO = new UserDTO();
        expectedDTO.setId(userId);
        expectedDTO.setName("John");

        when(userRepository.findUserById(userId)).thenReturn(user);
        when(userMapper.toDTO(user)).thenReturn(expectedDTO);

        UserDTO result = userService.getUserById(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("John", result.getName());
    }

    @Test
    void testGetUserById_WhenUserNotExists_ShouldThrowException() {

        Long userId = 999L;
        when(userRepository.findUserById(userId)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> userService.getUserById(userId));
    }

    @Test
    void testGetAllUsers_ShouldReturnPageOfUserDTOs() {

        Pageable pageable = PageRequest.of(0, 10);
        User user = new User();
        user.setId(1L);
        Page<User> userPage = new PageImpl<>(List.of(user));

        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);

        when(userRepository.findAll(pageable)).thenReturn(userPage);
        when(userMapper.toDTO(user)).thenReturn(userDTO);

        Page<UserDTO> result = userService.getAllUsers(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(userRepository).findAll(pageable);
    }

    @Test
    void testGetUsersWithFilters_ShouldReturnFilteredUsers() {

        Pageable pageable = PageRequest.of(0, 10);
        String firstName = "John";
        String surname = "Doe";

        User user = new User();
        user.setId(1L);
        Page<User> userPage = new PageImpl<>(List.of(user));

        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);
        when(userMapper.toDTO(user)).thenReturn(userDTO);

        Page<UserDTO> result = userService.getUsersWithFilters(firstName, surname, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(userRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testUpdateUser_ValidData_ShouldReturnUpdatedUserDTO() {

        Long userId = 1L;
        UserDTO updateDTO = new UserDTO();
        updateDTO.setName("Updated");
        updateDTO.setSurname("Name");
        updateDTO.setBirthDate(LocalDate.of(1990, 1, 1));
        updateDTO.setEmail("updated@example.com");

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setEmail("old@example.com");

        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setName("Updated");
        updatedUser.setEmail("updated2@example.com");

        UserDTO expectedDTO = new UserDTO();
        expectedDTO.setId(userId);
        expectedDTO.setName("Updated");

        when(userRepository.findUserById(userId)).thenReturn(existingUser);
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(userRepository.updateUser(anyLong(), anyString(), anyString(), any(), anyString())).thenReturn(updatedUser);
        when(userRepository.findUserById(userId)).thenReturn(updatedUser);
        when(userMapper.toDTO(updatedUser)).thenReturn(expectedDTO);

        UserDTO result = userService.updateUser(userId, updateDTO);

        assertNotNull(result);
        assertEquals("Updated", result.getName());
        verify(userRepository).updateUser(userId, "Updated", "Name",
                                        updateDTO.getBirthDate(), "updated@example.com");
    }

    @Test
    void testDeleteUser_WhenUserExists_ShouldDeleteUser() {

        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        when(userRepository.findUserById(userId)).thenReturn(user);

        userService.deleteUser(userId);

        verify(userRepository).delete(user);
    }
}