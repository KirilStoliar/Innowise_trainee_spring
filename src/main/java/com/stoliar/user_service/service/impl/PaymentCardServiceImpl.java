package com.stoliar.user_service.service.impl;

import com.stoliar.user_service.dto.PaymentCardCreateDTO;
import com.stoliar.user_service.dto.PaymentCardDTO;
import com.stoliar.user_service.entity.PaymentCard;
import com.stoliar.user_service.entity.User;
import com.stoliar.user_service.exception.CustomExceptions;
import com.stoliar.user_service.mapper.PaymentCardMapper;
import com.stoliar.user_service.repository.PaymentCardRepository;
import com.stoliar.user_service.repository.UserRepository;
import com.stoliar.user_service.service.PaymentCardService;
import com.stoliar.user_service.specification.PaymentCardSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCardServiceImpl implements PaymentCardService {

    private final PaymentCardRepository paymentCardRepository;
    private final UserRepository userRepository;
    private final PaymentCardMapper paymentCardMapper;
    private final CacheManager cacheManager;

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public PaymentCardDTO createPaymentCard(Long userId, PaymentCardCreateDTO paymentCardCreateDTO) {
        log.info("Creating payment card for user id: {}", userId);

        // Проверка наличия пользователя и его активности
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new CustomExceptions.EntityNotFoundException("User not found with id: " + userId);
        }

        if (!user.getActive()) {
            throw new CustomExceptions.BusinessRuleException("Cannot add card to inactive user");
        }

        // Проверка лимита карт
        int currentCardCount = userRepository.countActiveCardsByUserId(userId);
        if (currentCardCount >= 5) {
            throw new CustomExceptions.BusinessRuleException("User cannot have more than 5 active cards. Current count: " + currentCardCount);
        }

        // Проверка уникального номера карты
        if (paymentCardRepository.findByNumber(paymentCardCreateDTO.getNumber()).isPresent()) {
            throw new CustomExceptions.DuplicateResourceException("Card with number " + paymentCardCreateDTO.getNumber() + " already exists");
        }

        PaymentCard createdCard = paymentCardRepository.createCard(
                userId,
                paymentCardCreateDTO.getNumber(),
                paymentCardCreateDTO.getHolder(),
                paymentCardCreateDTO.getExpirationDate()
        );

        return paymentCardMapper.toDTO(createdCard);
    }

    @Override
    @Transactional
    public PaymentCardDTO getCardById(Long cardId) {
        log.info("Fetching card by id: {}", cardId);
        PaymentCard card = paymentCardRepository.findById(cardId)
                .orElseThrow(() -> new CustomExceptions.EntityNotFoundException("Card not found with id: " + cardId));
        return paymentCardMapper.toDTO(card);
    }

    @Override
    @Transactional
    public List<PaymentCardDTO> getAllCardsByUserId(Long userId) {
        log.info("Fetching all cards for user id: {}", userId);
        List<PaymentCard> cards = paymentCardRepository.findAllByUserId(userId);
        return paymentCardMapper.toDTOList(cards);
    }

    @Override
    @Transactional
    public Page<PaymentCardDTO> getAllCards(Pageable pageable) {
        log.info("Fetching all cards with pagination");
        return paymentCardRepository.findAll(
                PaymentCardSpecifications.alwaysTrue(),
                pageable
        ).map(paymentCardMapper::toDTO);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#paymentCardDTO.userId")
    public PaymentCardDTO updateCard(Long cardId, PaymentCardDTO paymentCardDTO) {
        log.info("Updating card with id: {}", cardId);

        PaymentCard existingCard = paymentCardRepository.findById(cardId)
                .orElseThrow(() -> new CustomExceptions.EntityNotFoundException("Card not found with id: " + cardId));

        // Проверка уникальности номера карты
        if (!existingCard.getNumber().equals(paymentCardDTO.getNumber()) &&
                paymentCardRepository.findByNumber(paymentCardDTO.getNumber()).isPresent()) {
            throw new CustomExceptions.DuplicateResourceException("Card with number " + paymentCardDTO.getNumber() + " already exists");
        }

        existingCard.setNumber(paymentCardDTO.getNumber());
        existingCard.setHolder(paymentCardDTO.getHolder());
        existingCard.setExpirationDate(paymentCardDTO.getExpirationDate());

        PaymentCard updatedCard = paymentCardRepository.save(existingCard);

        return paymentCardMapper.toDTO(updatedCard);
    }

    @Override
    @Transactional
    public PaymentCardDTO updateCardStatus(Long cardId, boolean active) {
        log.info("Updating card status: {}", active);

        PaymentCard card = paymentCardRepository.findById(cardId)
                .orElseThrow(() -> new CustomExceptions.EntityNotFoundException("Card not found with id: " + cardId));

        card.setActive(active);
        PaymentCard updatedCard = paymentCardRepository.save(card);

        Long userId = updatedCard.getUser().getId();

        // Удаляем из кеша
        evictUserCache(userId);

        return paymentCardMapper.toDTO(updatedCard);
    }

    @Override
    @Transactional
    public void deleteCard(Long cardId) {
        log.info("Deleting card with id: {}", cardId);

        PaymentCard card = paymentCardRepository.findById(cardId)
                .orElseThrow(() -> new CustomExceptions.EntityNotFoundException("Card not found with id: " + cardId));
        Long userId = card.getUser().getId();

        paymentCardRepository.delete(card);

        // Удаляем из кеша
        evictUserCache(userId);
    }

    private void evictUserCache(Long userId) {
        Cache cache = cacheManager.getCache("users");
        if (cache != null) {
            cache.evict(userId);
        }
    }
}