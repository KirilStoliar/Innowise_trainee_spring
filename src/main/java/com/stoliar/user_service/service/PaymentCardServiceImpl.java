package com.stoliar.user_service.service;

import com.stoliar.user_service.dto.PaymentCardCreateDTO;
import com.stoliar.user_service.dto.PaymentCardDTO;
import com.stoliar.user_service.entity.PaymentCard;
import com.stoliar.user_service.entity.User;
import com.stoliar.user_service.mapper.PaymentCardMapper;
import com.stoliar.user_service.repository.PaymentCardRepository;
import com.stoliar.user_service.repository.UserRepository;
import com.stoliar.user_service.specification.PaymentCardSpecifications;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCardServiceImpl implements PaymentCardService {

    private final PaymentCardRepository paymentCardRepository;
    private final UserRepository userRepository;
    private final PaymentCardMapper paymentCardMapper;

    @Override
    @Transactional
    public PaymentCardDTO createPaymentCard(Long userId, PaymentCardCreateDTO paymentCardCreateDTO) {
        log.info("Creating payment card for user id: {}", userId);
        
        // Проверка наличия пользователя и его активности
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new EntityNotFoundException("User not found with id: " + userId);
        }
        
        if (!user.getActive()) {
            throw new IllegalStateException("Cannot add card to inactive user");
        }

        // Проверка лимита карт
        int currentCardCount = userRepository.countActiveCardsByUserId(userId);
        if (currentCardCount >= 5) {
            throw new IllegalStateException("User cannot have more than 5 active cards. Current count: " + currentCardCount);
        }

        // Проверка уникального номера карты
        if (paymentCardRepository.findByNumber(paymentCardCreateDTO.getNumber()).isPresent()) {
            throw new IllegalArgumentException("Card with number " + paymentCardCreateDTO.getNumber() + " already exists");
        }

        // Создание карты
        int created = paymentCardRepository.createCard(
            userId,
            paymentCardCreateDTO.getNumber(),
            paymentCardCreateDTO.getHolder(),
            paymentCardCreateDTO.getExpirationDate()
        );

        if (created != 1) {
            throw new RuntimeException("Failed to create payment card. Created records: " + created);
        }

        PaymentCard createdCard = paymentCardRepository.findByNumber(paymentCardCreateDTO.getNumber())
                .orElseThrow(() -> new RuntimeException("Card created but not found"));
        
        return paymentCardMapper.toDTO(createdCard);
    }

    @Override
    public PaymentCardDTO getCardById(Long cardId) {
        log.info("Fetching card by id: {}", cardId);
        Optional<PaymentCard> card = paymentCardRepository.findById(cardId);
        if (card.isEmpty()) {
            throw new EntityNotFoundException("Card not found with id: " + cardId);
        }
        return paymentCardMapper.toDTO(card.orElse(null));
    }

    @Override
    public List<PaymentCardDTO> getAllCardsByUserId(Long userId) {
        log.info("Fetching all cards for user id: {}", userId);
        List<PaymentCard> cards = paymentCardRepository.findAllByUserId(userId);
        return paymentCardMapper.toDTOList(cards);
    }

    @Override
    public Page<PaymentCardDTO> getAllCards(Pageable pageable) {
        log.info("Fetching all cards with pagination");
        return paymentCardRepository.findAll(
                PaymentCardSpecifications.alwaysTrue(), 
                pageable
        ).map(paymentCardMapper::toDTO);
    }

    @Override
    @Transactional
    public PaymentCardDTO updateCard(Long cardId, PaymentCardDTO paymentCardDTO) {
        log.info("Updating card with id: {}", cardId);
        
        Optional<PaymentCard> existingCard = paymentCardRepository.findById(cardId);
        if (existingCard.isEmpty()) {
            throw new EntityNotFoundException("Card not found with id: " + cardId);
        }

        // Проверка уникальности номера карты
        if (!existingCard.get().getNumber().equals(paymentCardDTO.getNumber()) &&
            paymentCardRepository.findByNumber(paymentCardDTO.getNumber()).isPresent()) {
            throw new IllegalArgumentException("Card with number " + paymentCardDTO.getNumber() + " already exists");
        }

        PaymentCard updated = paymentCardRepository.updatePaymentCard(
            cardId,
            paymentCardDTO.getNumber(),
            paymentCardDTO.getHolder(),
            paymentCardDTO.getExpirationDate()
        );
        
        if (!updated.getActive()) {
            throw new RuntimeException("Failed to update card with id: " + cardId);
        }

        Optional<PaymentCard> updatedCard = paymentCardRepository.findById(cardId);
        return paymentCardMapper.toDTO(updatedCard.orElse(null));
    }

    @Override
    @Transactional
    public void updateCardStatus(Long id, boolean active) {
        log.info("Updating card status: {}", active);
        PaymentCard card = paymentCardRepository.updateCardStatus(id, active);
        if (!card.getActive()) {
            throw new EntityNotFoundException("Card status can't update in " + active);
        }
    }

    @Override
    @Transactional
    public void deleteCard(Long cardId) {
        log.info("Deleting card with id: {}", cardId);
        PaymentCard card = paymentCardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Card not found with id: " + cardId));
        paymentCardRepository.delete(card);
    }
}