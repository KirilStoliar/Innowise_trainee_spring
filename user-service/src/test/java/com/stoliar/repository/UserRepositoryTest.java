package com.stoliar.repository;

import com.stoliar.entity.PaymentCard;
import com.stoliar.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("integration-test")
class UserRepositoryTest extends AbstractJpaTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testCreateUser_ShouldCreateAndReturnUser() {
        // Given
        String email = "john.doe" + UUID.randomUUID() + "@example.com";

        // When
        User createdUser = userRepository.createUser(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                email
        );

        // Then
        assertNotNull(createdUser);
        assertNotNull(createdUser.getId());
        assertEquals("John", createdUser.getName());
        assertEquals("Doe", createdUser.getSurname());
        assertTrue(createdUser.getActive());
        assertNotNull(createdUser.getCreatedAt());

        // Проверяем, что пользователь действительно сохранен в БД
        entityManager.clear();
        User foundUser = userRepository.findUserById(createdUser.getId());
        assertEquals("John", foundUser.getName());
    }

    @Test
    void testSave_ShouldUpdateUserData() {
        // Given - Создаем пользователя через нативный запрос
        User user = userRepository.createUser(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                "john.doe" + UUID.randomUUID() + "@example.com"
        );
        entityManager.flush();
        entityManager.clear();

        // When
        User managedUser = userRepository.findUserById(user.getId());
        managedUser.setName("Jane");
        managedUser.setSurname("Smith");
        managedUser.setEmail("jane.smith" + UUID.randomUUID() + "@example.com");
        userRepository.save(managedUser);
        entityManager.flush();
        entityManager.clear();

        // Then - Проверяем в БД
        User dbUser = userRepository.findUserById(user.getId());
        assertEquals("Jane", dbUser.getName());
        assertEquals("Smith", dbUser.getSurname());
    }

    @Test
    void testSave_ShouldUpdateUserStatus() {
        // Given
        User user = userRepository.createUser(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                "john.doe" + UUID.randomUUID() + "@example.com"
        );
        entityManager.flush();
        entityManager.clear();

        // When
        User managedUser = userRepository.findUserById(user.getId());
        managedUser.setActive(false);

        userRepository.save(managedUser);
        entityManager.flush();
        entityManager.clear();

        // Then
        User dbUser = userRepository.findUserById(user.getId());
        assertFalse(dbUser.getActive());
    }

    @Test
    void testFindUserById_WhenUserExists_ShouldReturnUser() {
        // Given
        User user = userRepository.createUser(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                "john.doe" + UUID.randomUUID() + "@example.com"
        );

        // When
        User foundUser = userRepository.findUserById(user.getId());

        // Then
        assertNotNull(foundUser);
        assertEquals(user.getId(), foundUser.getId());
        assertEquals("John", foundUser.getName());
    }

    @Test
    void testExistsByEmail_WhenEmailExists_ShouldReturnTrue() {
        // Given
        String email = "test" + UUID.randomUUID() + "@example.com";
        userRepository.createUser(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                email
        );

        // When
        boolean exists = userRepository.existsByEmail(email);

        // Then
        assertTrue(exists);
    }

    @Test
    void testCountActiveCardsByUserId_ShouldReturnCorrectCount() {
        // Given
        User user = userRepository.createUser(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                "john.doe" + UUID.randomUUID() + "@example.com"
        );
        entityManager.flush();

        // Создаем несколько карт для пользователя
        PaymentCard card1 = new PaymentCard();
        card1.setUser(user);
        card1.setNumber("1111222233334444");
        card1.setHolder("John Doe");
        card1.setExpirationDate(LocalDate.now().plusYears(2));
        card1.setActive(true);
        card1.setCreatedAt(LocalDateTime.now());
        entityManager.persist(card1);

        PaymentCard card2 = new PaymentCard();
        card2.setUser(user);
        card2.setNumber("5555666677778888");
        card2.setHolder("John Doe");
        card2.setExpirationDate(LocalDate.now().plusYears(3));
        card2.setActive(false); // Неактивная карта
        card2.setCreatedAt(LocalDateTime.now());
        entityManager.persist(card2);

        PaymentCard card3 = new PaymentCard();
        card3.setUser(user);
        card3.setNumber("9999888877776666");
        card3.setHolder("John Doe");
        card3.setExpirationDate(LocalDate.now().plusYears(4));
        card3.setActive(true);
        card3.setCreatedAt(LocalDateTime.now());
        entityManager.persist(card3);

        entityManager.flush();

        // When
        int activeCardCount = userRepository.countActiveCardsByUserId(user.getId());

        // Then
        assertEquals(2, activeCardCount); // Две активные карты
    }

    @Test
    void testCreateUser_WithDuplicateEmail_ShouldThrowException() {
        // Given
        String email = "duplicate" + UUID.randomUUID() + "@example.com";

        // Создаем первого пользователя
        userRepository.createUser(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                email
        );
        entityManager.flush();

        // When & Then - Пытаемся создать пользователя с тем же email
        assertThrows(Exception.class, () -> {
            userRepository.createUser(
                    "Jane",
                    "Smith",
                    LocalDate.of(1995, 1, 1),
                    email // Дубликат email
            );
            entityManager.flush();
        });
    }
}