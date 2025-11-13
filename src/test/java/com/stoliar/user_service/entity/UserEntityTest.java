package com.stoliar.user_service.entity;

import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserEntityTest {

    private final Validator validator;

    public UserEntityTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private User createTestUser() {
        User user = new User();
        user.setName("John");
        user.setSurname("Doe");gi
        user.setBirthDate(LocalDate.of(1990, 1, 1));
        user.setEmail("john.doe@example.com");
        user.setActive(true);
        return user;
    }

    @Test
    void testUserCreation_ValidData_ShouldPassValidation() {

        User user = createTestUser();

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.isEmpty(), "No validation violations should occur");
    }

    @Test
    void testUserCreation_InvalidEmail_ShouldFailValidation() {

        User user = createTestUser();
        user.setEmail("invalid-email");
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty(), "Validation should fail for invalid email");
        assertEquals(1, violations.size());
        assertEquals("Email should be valid", violations.iterator().next().getMessage());
    }

    @Test
    void testUserCreation_BlankName_ShouldFailValidation() {

        User user = createTestUser();
        user.setName("");

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty(), "Validation should fail for blank name");
    }

    @Test
    void testUserCreation_FutureBirthDate_ShouldFailValidation() {

        User user = createTestUser();
        user.setBirthDate(LocalDate.now().plusDays(1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty(), "Validation should fail for future birth date");
    }

}