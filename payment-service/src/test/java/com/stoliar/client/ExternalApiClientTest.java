package com.stoliar.client;

import com.stoliar.entity.enums.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ExternalApiClientTest {

    @Test
    void shouldReturnCompletedWhenFallbackRandomIsEven() {
        // Given
        RestTemplate restTemplate = mock(RestTemplate.class);
        Random random = mock(Random.class);
        when(random.nextInt(100)).thenReturn(1);

        ExternalApiClient client = new ExternalApiClient(restTemplate, random);
        ReflectionTestUtils.setField(client, "fallbackEnabled", true);

        // When
        PaymentStatus status = client.determinePaymentStatus();

        // Then
        assertEquals(PaymentStatus.COMPLETED, status);
    }
}