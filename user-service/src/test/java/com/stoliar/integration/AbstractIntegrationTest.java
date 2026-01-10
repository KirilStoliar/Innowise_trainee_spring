package com.stoliar.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@ActiveProfiles("integration-test")
@Testcontainers
@Import(TestIntegrationSecurityConfig.class)
public abstract class AbstractIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgresqlContainer = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:15-alpine")
    )
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL настройки
        registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresqlContainer::getUsername);
        registry.add("spring.datasource.password", postgresqlContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

        // Явно отключаем Liquibase и настраиваем JPA
        registry.add("spring.liquibase.enabled", () -> "false");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");

        // Отключаем Redis для тестов
        registry.add("spring.data.redis.enabled", () -> "false");
        registry.add("spring.cache.type", () -> "none");

        // Свойства, необходимые для UserController
        registry.add("api.gateway.internal-token", () -> "test-internal-token");
        registry.add("app.jwt.secret", () -> "test-jwt-secret-for-integration-tests");

        // Явно отключаем security для тестов
        registry.add("spring.security.enabled", () -> "false");
        registry.add("management.security.enabled", () -> "false");
    }
}