package com.stoliar.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stoliar.dto.PaymentCardCreateDTO;
import com.stoliar.entity.PaymentCard;
import com.stoliar.entity.User;
import com.stoliar.repository.PaymentCardRepository;
import com.stoliar.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
@Transactional
@Import(TestIntegrationSecurityConfig.class)
class PaymentCardControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentCardRepository paymentCardRepository;

    private User testUser;
    private PaymentCard testCard;

    @BeforeEach
    void setUp() {
        // Given - подготовка тестовых данных
        testUser = new User();
        testUser.setName("Card");
        testUser.setSurname("Test");
        testUser.setBirthDate(LocalDate.of(1990, 1, 1));
        testUser.setEmail("card.test@example.com");
        testUser.setActive(true);
        testUser = userRepository.save(testUser);

        testCard = new PaymentCard();
        testCard.setUser(testUser);
        testCard.setNumber("4111111111111111");
        testCard.setHolder("Test Holder");
        testCard.setExpirationDate(LocalDate.now().plusYears(2));
        testCard.setActive(true);
        testCard = paymentCardRepository.save(testCard);
    }

    @Test
    void createPaymentCard_ValidData_ShouldReturnCreated() throws Exception {
        // Given
        PaymentCardCreateDTO createDTO = new PaymentCardCreateDTO();
        createDTO.setNumber("5555555555554444");
        createDTO.setHolder("John Doe");
        createDTO.setExpirationDate(LocalDate.now().plusYears(2));

        // When & Then
        mockMvc.perform(post("/api/v1/users/{userId}/payment-cards", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.number", is("5555555555554444")))
                .andExpect(jsonPath("$.data.holder", is("John Doe")));
    }

    @Test
    void getCardById_WhenCardExists_ShouldReturnCard() throws Exception {
        // Given - testCard уже создан в setUp

        // When & Then
        mockMvc.perform(get("/api/v1/users/{userId}/payment-cards/{cardId}",
                testUser.getId(), testCard.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(testCard.getId().intValue())))
                .andExpect(jsonPath("$.data.number", is("4111111111111111")));
    }

    @Test
    void getAllCardsByUserId_ShouldReturnUserCards() throws Exception {
        // Given - testCard уже создан в setUp

        // When & Then
        mockMvc.perform(get("/api/v1/users/{userId}/payment-cards", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    void updateCardStatus_ShouldUpdateStatus() throws Exception {
        // Given - testCard уже создан в setUp

        // When & Then
        mockMvc.perform(patch("/api/v1/users/{userId}/payment-cards/{cardId}/status",
                        testUser.getId(), testCard.getId())
                        .param("active", "false"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(testCard.getId().intValue()))) // Проверяем, что вернулся объект
                .andExpect(jsonPath("$.data.active", is(false))) // Проверяем обновленный статус
                .andExpect(jsonPath("$.message", containsString("deactivated")));
    }

    @Test
    void deleteCard_ShouldDeleteCard() throws Exception {
        // Given - testCard уже создан в setUp

        // When & Then
        mockMvc.perform(delete("/api/v1/users/{userId}/payment-cards/{cardId}",
                testUser.getId(), testCard.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void createPaymentCard_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Given

        PaymentCardCreateDTO createDTO = new PaymentCardCreateDTO();
        createDTO.setNumber("123");
        createDTO.setHolder("");
        createDTO.setExpirationDate(LocalDate.now().minusDays(1));

        // When & Then
        mockMvc.perform(post("/api/v1/users/{userId}/payment-cards", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCardById_WhenCardNotExists_ShouldReturnNotFound() throws Exception {
        // Given - несуществующий cardId
        Long nonExistentCardId = 999L;

        // When & Then
        mockMvc.perform(get("/api/v1/users/{userId}/payment-cards/{cardId}",
                        testUser.getId(), nonExistentCardId))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCard_ValidData_ShouldReturnUpdatedCard() throws Exception {
        // Given
        String updateJson = """
            {
                "id": %d,
                "userId": %d,
                "number": "5555666677778888",
                "holder": "Updated Holder",
                "expirationDate": "%s",
                "active": true
            }
            """.formatted(testCard.getId(), testUser.getId(), LocalDate.now().plusYears(3));

        // When & Then
        mockMvc.perform(put("/api/v1/users/{userId}/payment-cards/{cardId}",
                        testUser.getId(), testCard.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.holder", is("Updated Holder")));
    }
}