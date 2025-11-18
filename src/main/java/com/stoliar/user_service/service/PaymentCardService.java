package com.stoliar.user_service.service;

import com.stoliar.user_service.dto.PaymentCardCreateDTO;
import com.stoliar.user_service.dto.PaymentCardDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PaymentCardService {
    PaymentCardDTO createPaymentCard(Long userId, PaymentCardCreateDTO paymentCardCreateDTO);
    PaymentCardDTO getCardById(Long cardId);
    List<PaymentCardDTO> getAllCardsByUserId(Long userId);
    Page<PaymentCardDTO> getAllCardsByUserId(Long userId, Pageable pageable);
    Page<PaymentCardDTO> getAllCards(Pageable pageable);
    PaymentCardDTO updateCard(Long cardId, PaymentCardDTO paymentCardDTO);
    PaymentCardDTO updateCardStatus(Long id, boolean active);

    void deleteCard(Long cardId);
}