package com.stoliar.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @Container
    protected static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Container
    protected static final KafkaContainer kafkaContainer = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.4.0"))
            .withExposedPorts(9093);

    protected static WireMockServer wireMockServer;

    @BeforeAll
    static void init() {
        wireMockServer = new WireMockServer(wireMockConfig().port(9090));
        wireMockServer.start();
    }

    @AfterAll
    static void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL свойства
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.driver-class-name",
                () -> "org.postgresql.Driver");

        // Kafka свойства
        registry.add("spring.kafka.bootstrap-servers",
                () -> String.format("%s:%s",
                        kafkaContainer.getHost(),
                        kafkaContainer.getFirstMappedPort()));

        registry.add("external.api.url",
                () -> "http://localhost:9090/integers");

        // Отключаем Liquibase в тестах
        registry.add("spring.liquibase.enabled", () -> false);

        // Настройка JPA для тестов
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.properties.hibernate.dialect",
                () -> "org.hibernate.dialect.PostgreSQLDialect");

        // Отключаем Security для тестов
        registry.add("spring.security.enabled", () -> false);

        // Устанавливаем часовой пояс UTC
        registry.add("spring.jpa.properties.hibernate.jdbc.time_zone", () -> "UTC");

        // Или для PostgreSQL
        registry.add("spring.datasource.hikari.connection-init-sql",
                () -> "SET TIME ZONE 'UTC'");
    }
}