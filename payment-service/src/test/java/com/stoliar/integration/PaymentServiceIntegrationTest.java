package com.stoliar.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stoliar.dto.PaymentRequest;
import com.stoliar.entity.Payment;
import com.stoliar.entity.enums.PaymentStatus;
import com.stoliar.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.profiles.active=test")
@AutoConfigureMockMvc
@WithMockUser(username = "admin", roles = {"ADMIN"})
@EnabledIfSystemProperty(named = "use.testcontainers", matches = "true")
class PaymentServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAllInBatch();
        paymentRepository.flush();
        if (wireMockServer != null) {
            wireMockServer.resetAll();
        }
    }

    @Test
    void createPayment_ExternalApiSuccess_ReturnsCompleted() throws Exception {
        // Given
        wireMockServer.stubFor(get(urlMatching("/integers.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/plain")
                        .withBody("42")));

        PaymentRequest request = PaymentRequest.builder()
                .orderId(1L)
                .userId(1L)
                .paymentAmount(new BigDecimal("100.50"))
                .build();

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.status").value("COMPLETED"));

        List<Payment> payments = paymentRepository.findAll();
        assertThat(payments).hasSize(1);
        assertThat(payments.get(0).getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }

    @Test
    void createPayment_ValidationError_ReturnsBadRequest() throws Exception {
        // Given
        PaymentRequest request = PaymentRequest.builder()
                .orderId(null) // Invalid
                .userId(1L)
                .paymentAmount(new BigDecimal("0.00")) // Invalid
                .build();

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));
    }

    @Test
    void getPaymentById_Exists_ReturnsPayment() throws Exception {
        // Given
        Payment payment = Payment.builder()
                .orderId(10L)
                .userId(5L)
                .status(PaymentStatus.COMPLETED)
                .paymentAmount(new BigDecimal("50.00"))
                .timestamp(LocalDateTime.now())
                .build();
        Payment saved = paymentRepository.save(payment);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/payments/{id}", saved.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value(saved.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.orderId").value(10L));
    }

    @Test
    void getPaymentsByUserId_ReturnsFilteredPayments() throws Exception {
        // Given
        Payment payment1 = Payment.builder()
                .orderId(1L)
                .userId(100L)
                .status(PaymentStatus.COMPLETED)
                .paymentAmount(new BigDecimal("100.00"))
                .timestamp(LocalDateTime.now())
                .build();

        Payment payment2 = Payment.builder()
                .orderId(2L)
                .userId(100L)
                .status(PaymentStatus.FAILED)
                .paymentAmount(new BigDecimal("200.00"))
                .timestamp(LocalDateTime.now())
                .build();

        Payment payment3 = Payment.builder()
                .orderId(3L)
                .userId(200L)
                .status(PaymentStatus.COMPLETED)
                .paymentAmount(new BigDecimal("300.00"))
                .timestamp(LocalDateTime.now())
                .build();

        paymentRepository.saveAll(List.of(payment1, payment2, payment3));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/payments/user/{userId}", 100L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.length()").value(2));
    }

    @Test
    void getTotalSumByDateRange_WithTimeZone() throws Exception {
        // Given
        ZonedDateTime payment1Time = ZonedDateTime.of(
                2025, 12, 18, 12, 0, 0, 0,
                ZoneId.of("Europe/Moscow") // UTC+3
        );

        ZonedDateTime payment2Time = ZonedDateTime.of(
                2025, 12, 20, 12, 0, 0, 0,
                ZoneId.of("Europe/Moscow")
        );

        ZonedDateTime payment3Time = ZonedDateTime.of(
                2025, 12, 10, 12, 0, 0, 0,
                ZoneId.of("Europe/Moscow")
        );

        LocalDateTime payment1Local = payment1Time.toLocalDateTime();
        LocalDateTime payment2Local = payment2Time.toLocalDateTime();
        LocalDateTime payment3Local = payment3Time.toLocalDateTime();

        insertPaymentWithTimestamp(1L, 1L, new BigDecimal("100.00"), payment1Local);
        insertPaymentWithTimestamp(2L, 2L, new BigDecimal("200.00"), payment2Local);
        insertPaymentWithTimestamp(3L, 3L, new BigDecimal("300.00"), payment3Local);

        LocalDateTime startRange = LocalDateTime.of(2025, 12, 17, 0, 0, 0);
        LocalDateTime endRange = LocalDateTime.of(2025, 12, 24, 23, 59, 59);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/payments/total")
                        .param("startDate", startRange.toString())
                        .param("endDate", endRange.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data").value(300.00));
    }

    @Test
    void searchPaymentsByCriteria_ReturnsFilteredResults() throws Exception {
        // Given
        Payment payment1 = Payment.builder()
                .orderId(1L)
                .userId(100L)
                .status(PaymentStatus.COMPLETED)
                .paymentAmount(new BigDecimal("100.00"))
                .timestamp(LocalDateTime.now())
                .build();

        Payment payment2 = Payment.builder()
                .orderId(1L)
                .userId(200L)
                .status(PaymentStatus.FAILED)
                .paymentAmount(new BigDecimal("200.00"))
                .timestamp(LocalDateTime.now())
                .build();

        paymentRepository.saveAll(List.of(payment1, payment2));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/payments/search")
                        .param("orderId", "1")
                        .param("status", "COMPLETED")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.length()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].userId").value(100L));
    }

    private void insertPaymentWithTimestamp(Long orderId, Long userId, BigDecimal amount, LocalDateTime timestamp) {
        String sql = "INSERT INTO payments (order_id, user_id, status, payment_amount, timestamp) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, orderId, userId, "COMPLETED", amount, timestamp);
    }
}