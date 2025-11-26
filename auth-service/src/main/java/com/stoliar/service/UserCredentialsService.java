//package com.stoliar.service;
//
//import com.stoliar.entity.UserCredentials;
//import com.stoliar.exception.InvalidCredentialsException;
//import com.stoliar.repository.UserCredentialsRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class UserCredentialsService {
//
//    private final UserCredentialsRepository userCredentialsRepository;
//
//    @PreAuthorize("hasRole('ADMIN')")
//    public List<UserCredentials> getAllUsers() {
//        return userCredentialsRepository.findAll();
//    }
//
//    @PreAuthorize("hasRole('ADMIN')")
//    @Transactional
//    public UserCredentials setUserActive(Long userId, boolean active) {
//        UserCredentials credentials = userCredentialsRepository.findById(userId)
//                .orElseThrow(() -> new InvalidCredentialsException("User not found with id: " + userId));
//
//        credentials.setActive(active);
//        UserCredentials updated = userCredentialsRepository.save(credentials);
//
//        log.info("User {} {}: {}", userId, active ? "activated" : "deactivated", updated.getEmail());
//        return updated;
//    }
//}