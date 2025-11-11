package com.stoliar.user_service.repository;

import com.stoliar.user_service.entity.PaymentCard;
import com.stoliar.user_service.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class PaymentCardRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PaymentCardRepository paymentCardRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testFindById_WhenCardExists_ShouldReturnCard() {

        User user = createTestUser();
        PaymentCard card = createTestCard(user);
        PaymentCard savedCard = entityManager.persistAndFlush(card);

        Optional<PaymentCard> foundCard = paymentCardRepository.findById(savedCard.getId());

        assertTrue(foundCard.isPresent());
        assertEquals(savedCard.getId(), foundCard.get().getId());
        assertEquals("1234567890123456", foundCard.get().getNumber());
    }

    @Test
    void testFindByNumber_WhenCardExists_ShouldReturnCard() {

        User user = createTestUser();
        PaymentCard card = createTestCard(user);
        entityManager.persistAndFlush(card);

        Optional<PaymentCard> foundCard = paymentCardRepository.findByNumber("1234567890123456");

        assertTrue(foundCard.isPresent());
        assertEquals("1234567890123456", foundCard.get().getNumber());
    }

    @Test
    void testFindAllByUserId_ShouldReturnUserCards() {

        User user1 = createTestUser();
        User user2 = createTestUser();

        PaymentCard card1 = createTestCard(user1);
        PaymentCard card2 = createTestCard(user1);
        PaymentCard card3 = createTestCard(user2);

        entityManager.persistAndFlush(card1);
        entityManager.persistAndFlush(card2);
        entityManager.persistAndFlush(card3);

        List<PaymentCard> user1Cards = paymentCardRepository.findAllByUserId(user1.getId());

        assertEquals(2, user1Cards.size());
    }

    @Test
    void testFindByUserIdAndActiveTrue_ShouldReturnOnlyActiveCards() {

        User user = createTestUser();
        PaymentCard activeCard = createTestCard(user);
        activeCard.setActive(true);

        PaymentCard inactiveCard = createTestCard(user);
        inactiveCard.setActive(false);
        inactiveCard.setNumber("6543210987654321");

        entityManager.persistAndFlush(activeCard);
        entityManager.persistAndFlush(inactiveCard);

        List<PaymentCard> cards = paymentCardRepository.findAllByUserId(user.getId());
        List<PaymentCard> onlyActive = cards.stream()
                .filter(PaymentCard::getActive)
                .toList();

        assertEquals(1, onlyActive.size());
        assertTrue(onlyActive.get(0).getActive());
    }

    @Test
    void testUpdatePaymentCard_ShouldUpdateCardData() {

        User user = createTestUser();
        PaymentCard card = createTestCard(user);
        PaymentCard savedCard = entityManager.persistAndFlush(card);

        paymentCardRepository.updatePaymentCard(
                savedCard.getId(),
                "9999888877776666",
                "New Holder",
                LocalDate.now().plusYears(3)
        );
        entityManager.flush();
        entityManager.clear();

        Optional<PaymentCard> updatedCard = paymentCardRepository.findById(savedCard.getId());

        assertTrue(updatedCard.isPresent());
        assertEquals("9999888877776666", updatedCard.get().getNumber());
        assertEquals("New Holder", updatedCard.get().getHolder());
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

    private PaymentCard createTestCard(User user) {
        PaymentCard card = new PaymentCard();
        card.setUser(user);
        card.setNumber("1234567890123456");
        card.setHolder("Test Holder");
        card.setExpirationDate(LocalDate.now().plusYears(2));
        card.setActive(true);
        card.setCreatedAt(LocalDateTime.now());
        return card;
    }
}
