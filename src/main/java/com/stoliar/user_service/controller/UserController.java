package com.stoliar.user_service.controller;

import com.stoliar.user_service.dto.UserCreateDTO;
import com.stoliar.user_service.dto.UserDTO;
import com.stoliar.user_service.exception.ApiResponse;
import com.stoliar.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@Validated
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<UserDTO>> createUser(@Valid @RequestBody UserCreateDTO userCreateDTO) {
        log.info("Creating new user with email: {}", userCreateDTO.getEmail());

        UserDTO createdUser = userService.createUser(userCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdUser, "User created successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        log.info("Fetching user by id: {}", id);

        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user, "User retrieved successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserDTO>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort) {
        
        log.info("Fetching all users - page: {}, size: {}, sort: {}", page, size, sort);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        Page<UserDTO> users = userService.getAllUsers(pageable);
        
        return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
    }

    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<Page<UserDTO>>> getUsersWithFilters(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String surname,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Filtering users - firstName: {}, surname: {}", firstName, surname);
        Pageable pageable = PageRequest.of(page, size);
        Page<UserDTO> users = userService.getUsersWithFilters(firstName, surname, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(users, "Filtered users retrieved successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(
            @PathVariable Long id, 
            @Valid @RequestBody UserDTO userDTO) {
        
        log.info("Updating user with id: {}", id);

        UserDTO updatedUser = userService.updateUser(id, userDTO);
        return ResponseEntity.ok(ApiResponse.success(updatedUser, "User updated successfully"));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateUserStatus(
            @PathVariable Long id, 
            @RequestParam boolean active) {
        
        log.info("Updating user status - id: {}, active: {}", id, active);

        userService.updateUserStatus(id, active);
        String message = active ? "User activated successfully" : "User deactivated successfully";
        return ResponseEntity.ok(ApiResponse.success(null, message));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        log.info("Deleting user with id: {}", id);

        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully"));
    }
}