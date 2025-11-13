package com.stoliar.user_service.integration;

import com.stoliar.user_service.config.TestRedisConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Import(TestRedisConfig.class) // Импортируем тестовую конфигурацию Redis
@Testcontainers
public abstract class AbstractIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgresqlContainer = new PostgreSQLContainer<>(
        DockerImageName.parse("postgres:15-alpine")
    )
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL настройки
        registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresqlContainer::getUsername);
        registry.add("spring.datasource.password", postgresqlContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        
        // Redis настройки - указываем несуществующий порт
        registry.add("spring.data.redis.host", () -> "localhost");
        registry.add("spring.data.redis.port", () -> 16379);
        registry.add("spring.data.redis.timeout", () -> "1000ms");
    }
}