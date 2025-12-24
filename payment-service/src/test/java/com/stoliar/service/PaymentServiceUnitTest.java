package com.stoliar.service;

import com.stoliar.client.ExternalApiClient;
import com.stoliar.dto.PaymentRequest;
import com.stoliar.dto.PaymentResponse;
import com.stoliar.entity.Payment;
import com.stoliar.entity.enums.PaymentStatus;
import com.stoliar.mapper.PaymentMapper;
import com.stoliar.repository.PaymentRepository;
import com.stoliar.service.kafka.PaymentEventProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceUnitTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private ExternalApiClient externalApiClient;

    @Mock
    private PaymentEventProducer paymentEventProducer;

    @InjectMocks
    private PaymentService paymentService;

    private Payment payment;
    private PaymentRequest paymentRequest;
    private PaymentResponse paymentResponse;

    @BeforeEach
    void setUp() {
        payment = Payment.builder()
                .id(1L)
                .orderId(100L)
                .userId(50L)
                .status(PaymentStatus.COMPLETED)
                .timestamp(LocalDateTime.now())
                .paymentAmount(new BigDecimal("150.75"))
                .build();

        paymentRequest = PaymentRequest.builder()
                .orderId(100L)
                .userId(50L)
                .paymentAmount(new BigDecimal("150.75"))
                .build();

        paymentResponse = PaymentResponse.builder()
                .id(1L)
                .orderId(100L)
                .userId(50L)
                .status(PaymentStatus.COMPLETED)
                .timestamp(LocalDateTime.now())
                .paymentAmount(new BigDecimal("150.75"))
                .build();
    }

    @Test
    void createPayment_Success() {
        // Given
        when(paymentMapper.toEntity(paymentRequest)).thenReturn(payment);
        when(externalApiClient.determinePaymentStatus()).thenReturn(PaymentStatus.COMPLETED);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(paymentMapper.toResponse(payment)).thenReturn(paymentResponse);

        // When
        PaymentResponse result = paymentService.createPayment(paymentRequest);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(PaymentStatus.COMPLETED, result.getStatus());
        verify(paymentRepository, times(2)).save(any(Payment.class));
        verify(paymentEventProducer, times(1)).sendPaymentCreatedEvent(payment);
    }

    @Test
    void createPayment_ExternalApiFailed_Fallback() {
        // Given
        when(paymentMapper.toEntity(paymentRequest)).thenReturn(payment);
        when(externalApiClient.determinePaymentStatus()).thenReturn(PaymentStatus.FAILED);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(paymentMapper.toResponse(payment)).thenReturn(paymentResponse);
        payment.setStatus(PaymentStatus.FAILED);

        // When
        PaymentResponse result = paymentService.createPayment(paymentRequest);

        // Then
        assertNotNull(result);
        verify(paymentRepository, times(2)).save(any(Payment.class));
        verify(paymentEventProducer, times(1)).sendPaymentCreatedEvent(payment);
    }

    @Test
    void getPaymentById_Success() {
        // Given
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentMapper.toResponse(payment)).thenReturn(paymentResponse);

        // When
        PaymentResponse result = paymentService.getPaymentById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(paymentRepository, times(1)).findById(1L);
    }

    @Test
    void getPaymentById_NotFound() {
        // Given
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> paymentService.getPaymentById(999L));
        verify(paymentRepository, times(1)).findById(999L);
    }

    @Test
    void getPaymentsByUserId_Success() {
        // Given
        List<Payment> payments = Arrays.asList(payment);
        when(paymentRepository.findByUserId(50L)).thenReturn(payments);
        when(paymentMapper.toResponse(payment)).thenReturn(paymentResponse);

        // When
        List<PaymentResponse> results = paymentService.getPaymentsByUserId(50L);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        verify(paymentRepository, times(1)).findByUserId(50L);
    }

    @Test
    void getPaymentsByOrderId_Success() {
        // Given
        List<Payment> payments = Arrays.asList(payment);
        when(paymentRepository.findByOrderId(100L)).thenReturn(payments);
        when(paymentMapper.toResponse(payment)).thenReturn(paymentResponse);

        // When
        List<PaymentResponse> results = paymentService.getPaymentsByOrderId(100L);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        verify(paymentRepository, times(1)).findByOrderId(100L);
    }

    @Test
    void getPaymentsByStatus_Success() {
        // Given
        List<Payment> payments = Arrays.asList(payment);
        when(paymentRepository.findByStatus(PaymentStatus.COMPLETED)).thenReturn(payments);
        when(paymentMapper.toResponse(payment)).thenReturn(paymentResponse);

        // When
        List<PaymentResponse> results = paymentService.getPaymentsByStatus(PaymentStatus.COMPLETED);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        verify(paymentRepository, times(1)).findByStatus(PaymentStatus.COMPLETED);
    }

    @Test
    void getPaymentsByCriteria_AllParameters() {
        // Given
        List<Payment> payments = Arrays.asList(payment);
        when(paymentRepository.findPaymentsByCriteria(50L, 100L, PaymentStatus.COMPLETED))
                .thenReturn(payments);
        when(paymentMapper.toResponse(payment)).thenReturn(paymentResponse);

        // When
        List<PaymentResponse> results = paymentService.getPaymentsByCriteria(50L, 100L, PaymentStatus.COMPLETED);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    void getPaymentsByCriteria_NullParameters() {
        // Given
        List<Payment> payments = Arrays.asList(payment);
        when(paymentRepository.findPaymentsByCriteria(null, null, null))
                .thenReturn(payments);
        when(paymentMapper.toResponse(payment)).thenReturn(paymentResponse);

        // When
        List<PaymentResponse> results = paymentService.getPaymentsByCriteria(null, null, null);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    void getTotalSumByUserIdAndDateRange_Success() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        when(paymentRepository.getTotalSumByUserIdAndDateRange(50L, startDate, endDate))
                .thenReturn(new BigDecimal("300.50"));

        // When
        BigDecimal result = paymentService.getTotalSumByUserIdAndDateRange(50L, startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("300.50"), result);
    }

    @Test
    void getTotalSumByDateRange_Success() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        when(paymentRepository.getTotalSumByDateRange(startDate, endDate))
                .thenReturn(new BigDecimal("1500.75"));

        // When
        BigDecimal result = paymentService.getTotalSumByDateRange(startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("1500.75"), result);
    }
}