package com.stoliar.integration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

@TestConfiguration
@EnableWebSecurity
public class TestIntegrationSecurityConfig {

    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                .anyRequest().permitAll() // Разрешаем все запросы без аутентификации
            );
        
        // Устанавливаем мок аутентификацию для всех тестов
        setupMockAuthentication();
        
        return http.build();
    }

    private void setupMockAuthentication() {
        // Создаем мок аутентификации с правами ADMIN
        var authorities = Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_ADMIN")
        );
        
        var authentication = new UsernamePasswordAuthenticationToken(
            1L, // userId как principal (совпадает с testUser.getId())
            null,
            authorities
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}