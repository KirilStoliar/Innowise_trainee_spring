package com.stoliar.user_service.entity;

import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PaymentCardEntityTest {

    private final Validator validator;

    public PaymentCardEntityTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    public PaymentCard createTestPaymentCard() {
        PaymentCard card = new PaymentCard();
        card.setUser(createTestUser());
        card.setNumber("1234567890123456");
        card.setHolder("John Doe");
        card.setExpirationDate(LocalDate.now().plusYears(2));
        card.setActive(true);
        return card;
    }

    public User createTestUser() {
        User user = new User();
        user.setId(1L);
        return user;
    }

    @Test
    void testPaymentCardCreation_ValidData_ShouldPassValidation() {

        PaymentCard card = createTestPaymentCard();

        Set<ConstraintViolation<PaymentCard>> violations = validator.validate(card);

        assertTrue(violations.isEmpty(), "No validation violations should occur");
    }

    @Test
    void testPaymentCardCreation_ShortCardNumber_ShouldFailValidation() {

        PaymentCard card = createTestPaymentCard();
        card.setNumber("1234567890");

        Set<ConstraintViolation<PaymentCard>> violations = validator.validate(card);

        assertFalse(violations.isEmpty(), "Validation should fail for short card number");
    }

    @Test
    void testPaymentCardCreation_PastExpirationDate_ShouldFailValidation() {

        PaymentCard card = createTestPaymentCard();
        card.setExpirationDate(LocalDate.now().minusDays(1));

        Set<ConstraintViolation<PaymentCard>> violations = validator.validate(card);

        assertFalse(violations.isEmpty(), "Validation should fail for past expiration date");
    }

    @Test
    void testPaymentCardCreation_BlankHolder_ShouldFailValidation() {

        PaymentCard card = createTestPaymentCard();
        card.setHolder("");

        Set<ConstraintViolation<PaymentCard>> violations = validator.validate(card);

        assertFalse(violations.isEmpty(), "Validation should fail for blank holder");
    }
}