package com.stoliar.user_service.repository;

import com.stoliar.user_service.entity.PaymentCard;
import com.stoliar.user_service.entity.User;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("integration-test")
class PaymentCardRepositoryTest extends AbstractJpaTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PaymentCardRepository paymentCardRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @Transactional
    void testCreateCard_ShouldCreateAndReturnCard() {
        User user = createTestUser();

        PaymentCard createdCard = paymentCardRepository.createCard(
                user.getId(),
                "1234567890123456",
                "Test Holder",
                LocalDate.now().plusYears(2)
        );

        assertNotNull(createdCard);
        assertNotNull(createdCard.getId());
        assertEquals("1234567890123456", createdCard.getNumber());

        // Принудительно синхронизируем с БД
        entityManager.flush();
        entityManager.clear();

        // Проверяем в БД
        PaymentCard dbCard = paymentCardRepository.findById(createdCard.getId()).orElseThrow();
        assertEquals("1234567890123456", dbCard.getNumber());
    }

    @Test
    @Transactional
    void testSave_ShouldUpdateCardData() {
        User user = createTestUser();

        // Создаем через нативный запрос
        PaymentCard card = paymentCardRepository.createCard(
                user.getId(),
                "1234567890123456",
                "Test Holder",
                LocalDate.now().plusYears(2)
        );
        entityManager.flush();

        // Очищаем контекст и загружаем заново как managed entity
        entityManager.clear();
        PaymentCard managedCard = paymentCardRepository.findById(card.getId()).orElseThrow();

        // Обновляем managed entity
        managedCard.setNumber("9999888877776666");
        managedCard.setHolder("New Holder");

        // Сохраняем изменения
        PaymentCard updatedCard = paymentCardRepository.save(managedCard);
        entityManager.flush();
        entityManager.clear();

        // Проверяем в БД
        PaymentCard dbCard = paymentCardRepository.findById(card.getId()).orElseThrow();
        assertEquals("9999888877776666", dbCard.getNumber());
        assertEquals("New Holder", dbCard.getHolder());
    }

    @Test
    @Transactional
    void testSave_ShouldUpdateCardStatus() {
        User user = createTestUser();

        PaymentCard card = paymentCardRepository.createCard(
                user.getId(),
                "1234567890123456",
                "Test Holder",
                LocalDate.now().plusYears(2)
        );
        entityManager.flush();
        entityManager.clear();

        PaymentCard managedCard = paymentCardRepository.findById(card.getId()).orElseThrow();
        managedCard.setActive(false);

        paymentCardRepository.save(managedCard);
        entityManager.flush();
        entityManager.clear();

        PaymentCard dbCard = paymentCardRepository.findById(card.getId()).orElseThrow();
        assertFalse(dbCard.getActive());
    }

    @Test
    void testFindById_WhenCardExists_ShouldReturnCard() {
        User user = createTestUser();

        PaymentCard card = paymentCardRepository.createCard(
                user.getId(),
                "1234567890123456",
                "Test Holder",
                LocalDate.now().plusYears(2)
        );

        Optional<PaymentCard> foundCard = paymentCardRepository.findById(card.getId());

        assertTrue(foundCard.isPresent());
        assertEquals(card.getId(), foundCard.get().getId());
        assertEquals("1234567890123456", foundCard.get().getNumber());
    }

    @Test
    void testFindByNumber_WhenCardExists_ShouldReturnCard() {
        User user = createTestUser();

        paymentCardRepository.createCard(
                user.getId(),
                "1234567890123456",
                "Test Holder",
                LocalDate.now().plusYears(2)
        );

        Optional<PaymentCard> foundCard = paymentCardRepository.findByNumber("1234567890123456");

        assertTrue(foundCard.isPresent());
        assertEquals("1234567890123456", foundCard.get().getNumber());
    }

    @Test
    void testFindAllByUserId_ShouldReturnUserCards() {
        User user1 = createTestUser();
        User user2 = createTestUser();

        paymentCardRepository.createCard(user1.getId(), "1111111111111111", "Holder1", LocalDate.now().plusYears(2));
        paymentCardRepository.createCard(user1.getId(), "2222222222222222", "Holder2", LocalDate.now().plusYears(3));
        paymentCardRepository.createCard(user2.getId(), "3333333333333333", "Holder3", LocalDate.now().plusYears(4));

        List<PaymentCard> user1Cards = paymentCardRepository.findAllByUserId(user1.getId());

        assertEquals(2, user1Cards.size());
    }

    @Test
    @Transactional
    void testCreateCard_WithDuplicateNumber_ShouldThrowException() {
        User user = createTestUser();

        paymentCardRepository.createCard(
                user.getId(),
                "1234567890123456",
                "Test Holder",
                LocalDate.now().plusYears(2)
        );
        entityManager.flush();

        // Пытаемся создать карту с тем же номером
        assertThrows(Exception.class, () -> {
            paymentCardRepository.createCard(
                    user.getId(),
                    "1234567890123456", // Дубликат номера
                    "Another Holder",
                    LocalDate.now().plusYears(3)
            );
            entityManager.flush();
        });
    }

    @Test
    void testDelete_ShouldRemoveCard() {
        User user = createTestUser();

        PaymentCard card = paymentCardRepository.createCard(
                user.getId(),
                "1234567890123456",
                "Test Holder",
                LocalDate.now().plusYears(2)
        );

        paymentCardRepository.deleteById(card.getId());
        entityManager.flush();
        entityManager.clear();

        Optional<PaymentCard> deletedCard = paymentCardRepository.findById(card.getId());
        assertFalse(deletedCard.isPresent());
    }

    private User createTestUser() {
        User user = new User();
        user.setName("Test");
        user.setSurname("User");
        user.setBirthDate(LocalDate.of(1990, 1, 1));
        user.setEmail("test" + UUID.randomUUID() + "@example.com");
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return entityManager.persistAndFlush(user);
    }
}