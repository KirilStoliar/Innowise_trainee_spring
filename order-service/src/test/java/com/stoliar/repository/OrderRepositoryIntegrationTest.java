package com.stoliar.repository;

import com.stoliar.entity.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@ActiveProfiles("test")
class OrderRepositoryIntegrationTest {

    private static final boolean USE_TESTCONTAINERS =
            Boolean.parseBoolean(System.getProperty("use.testcontainers", "true"));

    @Container
    static PostgreSQLContainer<?> postgres =
            USE_TESTCONTAINERS
                    ? new PostgreSQLContainer<>("postgres:15-alpine")
                        .withDatabaseName("testdb")
                        .withUsername("test")
                        .withPassword("test")
                    : null;

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        if (USE_TESTCONTAINERS) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl);
            registry.add("spring.datasource.username", postgres::getUsername);
            registry.add("spring.datasource.password", postgres::getPassword);
            registry.add("spring.datasource.driver-class-name",
                    () -> "org.postgresql.Driver");
        }
    }

    @Autowired
    OrderRepository orderRepository;

    @Test
    void saveOrder_ShouldPersistToDatabase() {
        // Arrange
        Order order = new Order();
        order.setUserId(1L);
        order.setEmail("test@example.com");
        order.setStatus(Order.OrderStatus.PENDING);
        order.setTotalPrice(100.0);
        order.setDeleted(false);

        // Act
        Order saved = orderRepository.save(order);

        // Assert
        assertThat(saved.getId()).isNotNull();

        Optional<Order> found = orderRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }
}
