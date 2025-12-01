package com.stoliar.service;

import com.stoliar.entity.UserCredentials;
import com.stoliar.repository.UserCredentialsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserCredentialsRepository userCredentialsRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserCredentials credentials = userCredentialsRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", email);
                    return new UsernameNotFoundException("User not found: " + email);
                });

        if (!credentials.getActive()) {
            log.warn("User account is disabled: {}", email);
            throw new UsernameNotFoundException("User account is disabled: " + email);
        }

        return new User(
                credentials.getEmail(),
                credentials.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + credentials.getRole().name()))
        );
    }
}