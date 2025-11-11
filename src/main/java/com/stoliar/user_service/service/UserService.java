package com.stoliar.user_service.service;

import com.stoliar.user_service.dto.UserCreateDTO;
import com.stoliar.user_service.dto.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserDTO createUser(UserCreateDTO userCreateDTO);
    UserDTO getUserById(Long id);
    Page<UserDTO> getAllUsers(Pageable pageable);
    UserDTO updateUser(Long id, UserDTO userDTO);
    void updateUserStatus(Long id, boolean active);
    void deleteUser(Long id);
    Page<UserDTO> getUsersWithFilters(String firstName, String surname, Pageable pageable);
}