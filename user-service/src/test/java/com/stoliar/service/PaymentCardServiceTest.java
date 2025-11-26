package com.stoliar.service;

import com.stoliar.dto.PaymentCardCreateDTO;
import com.stoliar.dto.PaymentCardDTO;
import com.stoliar.entity.PaymentCard;
import com.stoliar.entity.User;
import com.stoliar.mapper.PaymentCardMapper;
import com.stoliar.repository.PaymentCardRepository;
import com.stoliar.repository.UserRepository;
import com.stoliar.service.impl.PaymentCardServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class PaymentCardServiceTest {

    @Mock
    private PaymentCardRepository paymentCardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentCardMapper paymentCardMapper;

    @InjectMocks
    private PaymentCardServiceImpl paymentCardService;

    @Test
    void testCreatePaymentCard_ValidData_ShouldReturnCardDTO() {
        // Given
        Long userId = 1L;
        PaymentCardCreateDTO createDTO = new PaymentCardCreateDTO();
        createDTO.setNumber("1234567890123456");
        createDTO.setHolder("John Doe");
        createDTO.setExpirationDate(LocalDate.now().plusYears(2));

        User user = new User();
        user.setId(userId);
        user.setActive(true);

        PaymentCard card = new PaymentCard();
        card.setId(1L);
        card.setUser(user);
        card.setNumber("1234567890123456");
        card.setHolder("John Doe");
        card.setExpirationDate(createDTO.getExpirationDate());

        PaymentCardDTO expectedDTO = new PaymentCardDTO();
        expectedDTO.setId(1L);
        expectedDTO.setNumber("1234567890123456");
        expectedDTO.setUserId(userId);

        when(userRepository.findUserById(userId)).thenReturn(user);
        when(userRepository.countActiveCardsByUserId(userId)).thenReturn(2);
        when(paymentCardRepository.findByNumber("1234567890123456")).thenReturn(Optional.empty());
        when(paymentCardRepository.createCard(
                eq(userId),
                eq("1234567890123456"),
                eq("John Doe"),
                eq(createDTO.getExpirationDate())
        )).thenReturn(card);
        when(paymentCardMapper.toDTO(card)).thenReturn(expectedDTO);

        // When
        PaymentCardDTO result = paymentCardService.createPaymentCard(userId, createDTO);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("1234567890123456", result.getNumber());

        verify(paymentCardRepository).createCard(
                userId,
                "1234567890123456",
                "John Doe",
                createDTO.getExpirationDate()
        );
    }

    @Test
    void testUpdateCard_ValidData_ShouldReturnUpdatedCardDTO() {
        // Given
        Long cardId = 1L;

        PaymentCardDTO updateDTO = new PaymentCardDTO();
        updateDTO.setNumber("9999888877776666");
        updateDTO.setHolder("New Holder");
        updateDTO.setExpirationDate(LocalDate.now().plusYears(3));

        User user = new User();
        user.setId(1L);

        PaymentCard existingCard = new PaymentCard();
        existingCard.setId(cardId);
        existingCard.setUser(user);
        existingCard.setNumber("1234567890123456");
        existingCard.setHolder("Old Holder");

        PaymentCard updatedCard = new PaymentCard();
        updatedCard.setId(cardId);
        updatedCard.setUser(user);
        updatedCard.setNumber("9999888877776666");
        updatedCard.setHolder("New Holder");

        PaymentCardDTO expectedDTO = new PaymentCardDTO();
        expectedDTO.setId(cardId);
        expectedDTO.setNumber("9999888877776666");
        expectedDTO.setUserId(user.getId());

        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
        when(paymentCardRepository.findByNumber("9999888877776666")).thenReturn(Optional.empty());
        when(paymentCardRepository.save(any(PaymentCard.class))).thenReturn(updatedCard);
        when(paymentCardMapper.toDTO(updatedCard)).thenReturn(expectedDTO);

        // When
        PaymentCardDTO result = paymentCardService.updateCard(cardId, updateDTO);

        // Then
        assertNotNull(result);
        assertEquals(cardId, result.getId());
        assertEquals("9999888877776666", result.getNumber());
    }

    @Test
    void testUpdateCardStatus_WhenCardExists_ShouldUpdateStatus() {
        // Given
        Long cardId = 1L;
        User user = new User();
        user.setId(1L);

        PaymentCard existingCard = new PaymentCard();
        existingCard.setId(cardId);
        existingCard.setUser(user);
        existingCard.setActive(true);

        PaymentCard updatedCard = new PaymentCard();
        updatedCard.setId(cardId);
        updatedCard.setUser(user);
        updatedCard.setActive(false);

        PaymentCardDTO expectedDTO = new PaymentCardDTO();
        expectedDTO.setId(cardId);
        expectedDTO.setActive(false);
        expectedDTO.setUserId(user.getId());

        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
        when(paymentCardRepository.save(any(PaymentCard.class))).thenReturn(updatedCard);
        when(paymentCardMapper.toDTO(updatedCard)).thenReturn(expectedDTO);

        // When
        PaymentCardDTO result = paymentCardService.updateCardStatus(cardId, false);

        // Then
        assertNotNull(result);
        assertFalse(result.getActive());

        verify(paymentCardRepository).save(any(PaymentCard.class));
    }

    @Test
    void testGetCardById_WhenCardExists_ShouldReturnCardDTO() {
        // Given
        Long cardId = 1L;
        PaymentCard card = new PaymentCard();
        card.setId(cardId);

        PaymentCardDTO expectedDTO = new PaymentCardDTO();
        expectedDTO.setId(cardId);

        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(paymentCardMapper.toDTO(card)).thenReturn(expectedDTO);

        // When
        PaymentCardDTO result = paymentCardService.getCardById(cardId);

        // Then
        assertNotNull(result);
        assertEquals(cardId, result.getId());
    }

    @Test
    void testGetAllCardsByUserId_ShouldReturnCardDTOs() {
        // Given
        Long userId = 1L;
        PaymentCard card = new PaymentCard();
        card.setId(1L);

        PaymentCardDTO cardDTO = new PaymentCardDTO();
        cardDTO.setId(1L);

        when(paymentCardRepository.findAllByUserId(userId)).thenReturn(List.of(card));
        when(paymentCardMapper.toDTOList(List.of(card))).thenReturn(List.of(cardDTO));

        // When
        List<PaymentCardDTO> result = paymentCardService.getAllCardsByUserId(userId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void testDeleteCard_WhenCardExists_ShouldDeleteCard() {
        // Given
        Long cardId = 1L;
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        PaymentCard card = new PaymentCard();
        card.setId(cardId);
        card.setUser(user);

        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(card));

        // When
        paymentCardService.deleteCard(user.getId(), cardId);

        // Then
        verify(paymentCardRepository).delete(card);
    }

    @Test
    void testGetAllCards_ShouldReturnPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        PaymentCard card = new PaymentCard();
        card.setId(1L);

        Page<PaymentCard> cardPage = new PageImpl<>(List.of(card));

        PaymentCardDTO cardDTO = new PaymentCardDTO();
        cardDTO.setId(1L);

        when(paymentCardRepository.findAll((Specification<PaymentCard>) any(), eq(pageable))).thenReturn(cardPage);
        when(paymentCardMapper.toDTO(card)).thenReturn(cardDTO);

        // When
        Page<PaymentCardDTO> result = paymentCardService.getAllCards(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }
}