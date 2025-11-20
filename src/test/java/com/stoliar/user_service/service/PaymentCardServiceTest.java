package com.stoliar.user_service.service;

import com.stoliar.user_service.dto.PaymentCardCreateDTO;
import com.stoliar.user_service.dto.PaymentCardDTO;
import com.stoliar.user_service.entity.PaymentCard;
import com.stoliar.user_service.entity.User;
import com.stoliar.user_service.exception.CustomExceptions;
import com.stoliar.user_service.mapper.PaymentCardMapper;
import com.stoliar.user_service.repository.PaymentCardRepository;
import com.stoliar.user_service.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @Test
    void testCreatePaymentCard_ValidData_ShouldReturnCardDTO() {

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
        card.setUser(user);

        when(userRepository.findUserById(userId)).thenReturn(user);
        when(userRepository.countActiveCardsByUserId(userId)).thenReturn(2);

        // ВАЖНО: используем ПОРЯДОК ответов
        when(paymentCardRepository.findByNumber("1234567890123456"))
                .thenReturn(Optional.empty(), Optional.of(card));

        when(paymentCardRepository.createCard(
                eq(userId),
                eq("1234567890123456"),
                eq("John Doe"),
                eq(createDTO.getExpirationDate())
        )).thenReturn(1);

        when(paymentCardMapper.toDTO(card)).thenReturn(expectedDTO);

        PaymentCardDTO result = paymentCardService.createPaymentCard(userId, createDTO);

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
    void testCreatePaymentCard_UserNotExists_ShouldThrowException() {

        Long userId = 999L;
        PaymentCardCreateDTO createDTO = new PaymentCardCreateDTO();

        when(userRepository.findUserById(userId)).thenReturn(null);

        assertThrows(CustomExceptions.EntityNotFoundException.class, () -> paymentCardService.createPaymentCard(userId, createDTO));
    }

    @Test
    void testCreatePaymentCard_UserInactive_ShouldThrowException() {

        Long userId = 1L;
        PaymentCardCreateDTO createDTO = new PaymentCardCreateDTO();

        User user = new User();
        user.setId(userId);
        user.setActive(false);

        when(userRepository.findUserById(userId)).thenReturn(user);

        assertThrows(CustomExceptions.BusinessRuleException.class, () -> paymentCardService.createPaymentCard(userId, createDTO));
    }

    @Test
    void testCreatePaymentCard_CardLimitExceeded_ShouldThrowException() {

        Long userId = 1L;
        PaymentCardCreateDTO createDTO = new PaymentCardCreateDTO();

        User user = new User();
        user.setId(userId);
        user.setActive(true);

        when(userRepository.findUserById(userId)).thenReturn(user);
        when(userRepository.countActiveCardsByUserId(userId)).thenReturn(5);

        assertThrows(CustomExceptions.BusinessRuleException.class, () -> paymentCardService.createPaymentCard(userId, createDTO));
    }

    @Test
    void testCreatePaymentCard_DuplicateCardNumber_ShouldThrowException() {

        Long userId = 1L;
        PaymentCardCreateDTO createDTO = new PaymentCardCreateDTO();
        createDTO.setNumber("1234567890123456");

        User user = new User();
        user.setId(userId);
        user.setActive(true);

        PaymentCard existingCard = new PaymentCard();
        existingCard.setUser(user);

        when(userRepository.findUserById(userId)).thenReturn(user);
        when(userRepository.countActiveCardsByUserId(userId)).thenReturn(2);
        when(paymentCardRepository.findByNumber("1234567890123456")).thenReturn(Optional.of(existingCard));

        assertThrows(CustomExceptions.DuplicateResourceException.class, () -> paymentCardService.createPaymentCard(userId, createDTO));
    }

    @Test
    void testGetCardById_WhenCardExists_ShouldReturnCardDTO() {

        Long cardId = 1L;
        PaymentCard card = new PaymentCard();
        card.setId(cardId);

        PaymentCardDTO expectedDTO = new PaymentCardDTO();
        expectedDTO.setId(cardId);

        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(paymentCardMapper.toDTO(card)).thenReturn(expectedDTO);

        PaymentCardDTO result = paymentCardService.getCardById(cardId);

        assertNotNull(result);
        assertEquals(cardId, result.getId());
    }

    @Test
    void testGetAllCardsByUserId_ShouldReturnCardDTOs() {

        Long userId = 1L;
        PaymentCard card = new PaymentCard();
        card.setId(1L);

        PaymentCardDTO cardDTO = new PaymentCardDTO();
        cardDTO.setId(1L);

        when(paymentCardRepository.findAllByUserId(userId)).thenReturn(List.of(card));
        when(paymentCardMapper.toDTOList(List.of(card))).thenReturn(List.of(cardDTO));

        List<PaymentCardDTO> result = paymentCardService.getAllCardsByUserId(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void testDeleteCard_WhenCardExists_ShouldDeleteCard() {
        Long cardId = 1L;
        User user = new User();
        user.setId(1L);

        PaymentCard card = new PaymentCard();
        card.setId(cardId);
        card.setUser(user);

        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cacheManager.getCache("users")).thenReturn(cache);

        paymentCardService.deleteCard(cardId);

        verify(paymentCardRepository).delete(card);
        verify(cacheManager).getCache("users");
        verify(cache).evict(user.getId());
    }

    @Test
    void testUpdateCardStatus_WhenCardExists_ShouldUpdateStatus() {
        Long cardId = 1L;
        User user = new User();
        user.setId(1L);

        PaymentCard card = new PaymentCard();
        card.setId(cardId);
        card.setUser(user);
        card.setActive(true);

        PaymentCard updatedCard = new PaymentCard();
        updatedCard.setId(cardId);
        updatedCard.setUser(user);
        updatedCard.setActive(false);

        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(paymentCardRepository.updateCardStatus(cardId, false)).thenReturn(updatedCard);
        when(cacheManager.getCache("users")).thenReturn(cache);

        paymentCardService.updateCardStatus(cardId, false);

        verify(paymentCardRepository).updateCardStatus(cardId, false);
        verify(cacheManager).getCache("users");
        verify(cache).evict(user.getId());
    }

    @Test
    void testDeleteCard_WhenCacheNotFound_ShouldStillDeleteCard() {
        Long cardId = 1L;
        User user = new User();
        user.setId(1L);

        PaymentCard card = new PaymentCard();
        card.setId(cardId);
        card.setUser(user);

        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cacheManager.getCache("users")).thenReturn(null); // Кэш не найден

        paymentCardService.deleteCard(cardId);

        verify(paymentCardRepository).delete(card);
        verify(cacheManager).getCache("users");
        // Не должно быть вызова cache.evict() если кэш null
    }
}