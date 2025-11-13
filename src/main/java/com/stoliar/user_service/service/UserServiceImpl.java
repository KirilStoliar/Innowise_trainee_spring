package com.stoliar.user_service.service;

import com.stoliar.user_service.dto.UserCreateDTO;
import com.stoliar.user_service.dto.UserDTO;
import com.stoliar.user_service.entity.User;
import com.stoliar.user_service.exception.CustomExceptions;
import com.stoliar.user_service.mapper.UserMapper;
import com.stoliar.user_service.repository.UserRepository;
import com.stoliar.user_service.specification.UserSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDTO createUser(UserCreateDTO userCreateDTO) {
        log.info("Creating new user with email: {}", userCreateDTO.getEmail());

        if (userRepository.existsByEmail(userCreateDTO.getEmail())) {
            throw new CustomExceptions.DuplicateResourceException("User with email " + userCreateDTO.getEmail() + " already exists");
        }

        User user = userMapper.toEntity(userCreateDTO);
        user.setActive(true);

        User savedUser = userRepository.save(user);

        return userMapper.toDTO(savedUser);
    }

    @Override
    @Cacheable(value = "users", key = "#id")
    public UserDTO getUserById(Long id) {
        log.info("Fetching user by id: {}", id);
        User user = userRepository.findUserById(id);
        if (user == null) {
            throw new CustomExceptions.EntityNotFoundException("User not found with id: " + id);
        }
        return userMapper.toDTO(user);
    }

    @Override
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        log.info("Fetching all users with pagination");
        return userRepository.findAll(pageable)
                .map(userMapper::toDTO);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        log.info("Updating user with id: {}", id);
        
        User existingUser = userRepository.findUserById(id);
        if (existingUser == null) {
            throw new CustomExceptions.DuplicateResourceException("User not found with id: " + id);
        }

        // Проверка уникальности почты
        if (!existingUser.getEmail().equals(userDTO.getEmail()) && 
            userRepository.existsByEmail(userDTO.getEmail())) {
            throw new CustomExceptions.DuplicateResourceException("Email " + userDTO.getEmail() + " already exists");
        }

        User updated = userRepository.updateUser(
            id, 
            userDTO.getName(), 
            userDTO.getSurname(), 
            userDTO.getBirthDate(), 
            userDTO.getEmail()
        );
        
        if (updated == null) {
            throw new RuntimeException("Failed to update user with id: " + id);
        }

        User updatedUser = userRepository.findUserById(id);
        return userMapper.toDTO(updatedUser);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public UserDTO updateUserStatus(Long id, boolean active) {
        log.info("Updating user status: {}", active);
        User updateUser = userRepository.updateUserStatus(id, active);
        return userMapper.toDTO(updateUser);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(Long id) {
        log.info("Deleting user with id: {}", id);
        User user = userRepository.findUserById(id);
        if (user == null) {
            throw new CustomExceptions.EntityNotFoundException("User not found with id: " + id);
        }
        userRepository.delete(user);
    }

    @Override
    public Page<UserDTO> getUsersWithFilters(String firstName, String surname, Pageable pageable) {
        log.info("Fetching users with filters - firstName: {}, surname: {}", firstName, surname);

        Specification<User> spec = UserSpecifications.hasFirstName(firstName)
                .and(UserSpecifications.hasSurname(surname));

        return userRepository.findAll(spec, pageable).map(userMapper::toDTO);
    }
}