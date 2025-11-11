package com.stoliar.user_service.repository;

import com.stoliar.user_service.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testFindUserById_WhenUserExists_ShouldReturnUser() {

        User user = createTestUser("John", "Doe", "john.doe@example.com");
        User savedUser = entityManager.persistAndFlush(user);

        User foundUser = userRepository.findUserById(savedUser.getId());

        assertNotNull(foundUser);
        assertEquals(savedUser.getId(), foundUser.getId());
        assertEquals("John", foundUser.getName());
    }

    @Test
    void testFindUserById_WhenUserNotExists_ShouldReturnNull() {

        User foundUser = userRepository.findUserById(999L);

        assertNull(foundUser);
    }

    @Test
    void testExistsByEmail_WhenEmailExists_ShouldReturnTrue() {

        User user = createTestUser("John", "Doe", "existing@example.com");
        entityManager.persistAndFlush(user);

        boolean exists = userRepository.existsByEmail("existing@example.com");

        assertTrue(exists);
    }

    @Test
    void testExistsByEmail_WhenEmailNotExists_ShouldReturnFalse() {

        boolean exists = userRepository.existsByEmail("nonexisting@example.com");

        assertFalse(exists);
    }

    @Test
    void testFindByActiveTrue_ShouldReturnOnlyActiveUsers() {

        User activeUser = createTestUser("Active", "User", "active@example.com");
        activeUser.setActive(true);
        entityManager.persistAndFlush(activeUser);

        User inactiveUser = createTestUser("Inactive", "User", "inactive@example.com");
        inactiveUser.setActive(false);
        entityManager.persistAndFlush(inactiveUser);

        List<User> users = userRepository.findAll();
        List<User> activeUsers = users.stream()
                .filter(User::getActive)
                .toList();

        assertEquals(1, activeUsers.size());
        assertEquals("active@example.com", activeUsers.get(0).getEmail());
    }

    @Test
    void testUpdateUser_ShouldUpdateUserData() {

        User user = createTestUser("Old", "Name", "old@example.com");
        User savedUser = entityManager.persistAndFlush(user);

        User updated = userRepository.updateUser(
                savedUser.getId(),
                "New",
                "Name",
                LocalDate.of(1995, 5, 5),
                "new@example.com"
        );

        assertNotNull(updated);
        assertEquals("New", updated.getName());
        assertEquals("new@example.com", updated.getEmail());
    }

    @Test
    void testCountActiveCardsByUserId_ShouldReturnCorrectCount() {

        User user = createTestUser("John", "Doe", "john@example.com");
        User savedUser = entityManager.persistAndFlush(user);

        int count = userRepository.countActiveCardsByUserId(savedUser.getId());

        assertEquals(0, count);
    }

    private User createTestUser(String name, String surname, String email) {
        User user = new User();
        user.setName(name);
        user.setSurname(surname);
        user.setBirthDate(LocalDate.of(1990, 1, 1));
        user.setEmail(email);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }
}
