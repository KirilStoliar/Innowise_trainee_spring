package com.stoliar.service.impl;

import com.stoliar.dto.PaymentCardCreateDTO;
import com.stoliar.dto.PaymentCardDTO;
import com.stoliar.entity.PaymentCard;
import com.stoliar.entity.User;
import com.stoliar.exception.BusinessRuleException;
import com.stoliar.exception.DuplicateResourceException;
import com.stoliar.exception.EntityNotFoundException;
import com.stoliar.mapper.PaymentCardMapper;
import com.stoliar.repository.PaymentCardRepository;
import com.stoliar.repository.UserRepository;
import com.stoliar.service.PaymentCardService;
import com.stoliar.specification.PaymentCardSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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

    @Override
    @Transactional
    @CachePut(value = "paymentCards", key = "#result.id")
    public PaymentCardDTO createPaymentCard(Long userId, PaymentCardCreateDTO paymentCardCreateDTO) {
        log.info("Creating payment card for user id: {}", userId);

        // Проверка наличия пользователя и его активности
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new EntityNotFoundException("User not found with id: " + userId);
        }

        if (!user.getActive()) {
            throw new BusinessRuleException("Cannot add card to inactive user");
        }

        // Проверка лимита карт
        int currentCardCount = userRepository.countActiveCardsByUserId(userId);
        if (currentCardCount >= 5) {
            throw new BusinessRuleException("User cannot have more than 5 active cards. Current count: " + currentCardCount);
        }

        // Проверка уникального номера карты
        if (paymentCardRepository.findByNumber(paymentCardCreateDTO.getNumber()).isPresent()) {
            throw new DuplicateResourceException("Card with number " + paymentCardCreateDTO.getNumber() + " already exists");
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
    @Cacheable(value = "paymentCards", key = "#cardId")
    public PaymentCardDTO getCardById(Long cardId) {
        log.info("Fetching card by id: {}", cardId);
        PaymentCard card = paymentCardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Card not found with id: " + cardId));
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
    public Page<PaymentCardDTO> getAllCardsByUserId(Long userId, Pageable pageable) {
        log.info("Fetching paginated cards for user id: {}", userId);
        Specification<PaymentCard> spec = (root, query, cb) ->
                cb.equal(root.get("user").get("id"), userId);

        Page<PaymentCard> cardsPage = paymentCardRepository.findAll(spec, pageable);
        return cardsPage.map(paymentCardMapper::toDTO);
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
    @CachePut(value = "paymentCards", key = "#cardId")
    public PaymentCardDTO updateCard(Long cardId, PaymentCardDTO paymentCardDTO) {
        log.info("Updating card with id: {}", cardId);

        PaymentCard existingCard = paymentCardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Card not found with id: " + cardId));

        // Проверка уникальности номера карты
        if (!existingCard.getNumber().equals(paymentCardDTO.getNumber()) &&
                paymentCardRepository.findByNumber(paymentCardDTO.getNumber()).isPresent()) {
            throw new DuplicateResourceException("Card with number " + paymentCardDTO.getNumber() + " already exists");
        }

        existingCard.setNumber(paymentCardDTO.getNumber());
        existingCard.setHolder(paymentCardDTO.getHolder());
        existingCard.setExpirationDate(paymentCardDTO.getExpirationDate());

        PaymentCard updatedCard = paymentCardRepository.save(existingCard);

        return paymentCardMapper.toDTO(updatedCard);
    }

    @Override
    @Transactional
    @CachePut(value = "paymentCards", key = "#cardId")
    public PaymentCardDTO updateCardStatus(Long cardId, boolean active) {
        log.info("Updating card status: {}", active);

        PaymentCard card = paymentCardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Card not found with id: " + cardId));

        card.setActive(active);
        PaymentCard updatedCard = paymentCardRepository.save(card);

        return paymentCardMapper.toDTO(updatedCard);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "paymentCards", key = "#cardId"),
            @CacheEvict(value = "users", key = "#userId")
    })
    public void deleteCard(Long userId, Long cardId) {
        log.info("Deleting card with id: {}", cardId);

        PaymentCard card = paymentCardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Card not found with id: " + cardId));

        if (!card.getUser().getId().equals(userId)) {
            throw new EntityNotFoundException("Card not found for this user");
        }

        paymentCardRepository.delete(card);
    }
}