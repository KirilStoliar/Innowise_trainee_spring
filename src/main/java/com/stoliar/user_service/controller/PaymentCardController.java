package com.stoliar.user_service.controller;


import com.stoliar.user_service.dto.PaymentCardCreateDTO;
import com.stoliar.user_service.dto.PaymentCardDTO;
import com.stoliar.user_service.exception.EntityNotFoundException;
import com.stoliar.user_service.response.ApiResponse;
import com.stoliar.user_service.service.PaymentCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/{userId}/payment-cards")
@Validated
@Slf4j
@RequiredArgsConstructor
public class PaymentCardController {

    private final PaymentCardService paymentCardService;

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentCardDTO>> createPaymentCard(
            @PathVariable Long userId,
            @Valid @RequestBody PaymentCardCreateDTO paymentCardCreateDTO) {
        
        log.info("Creating payment card for user id: {}", userId);

        PaymentCardDTO createdCard = paymentCardService.createPaymentCard(userId, paymentCardCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdCard, "Payment card created successfully"));
    }

    @GetMapping("/{cardId}")
    public ResponseEntity<ApiResponse<PaymentCardDTO>> getCardById(
            @PathVariable Long userId,
            @PathVariable Long cardId) {
        
        log.info("Fetching card by id: {} for user id: {}", cardId, userId);

        // Проверяем, что карта принадлежит пользователю
        PaymentCardDTO card = checkCardOwnership(userId, cardId);

        return ResponseEntity.ok(ApiResponse.success(card, "Card retrieved successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentCardDTO>>> getAllCardsByUserId(@PathVariable Long userId) {
        log.info("Fetching all cards for user id: {}", userId);

        List<PaymentCardDTO> cards = paymentCardService.getAllCardsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(cards, "Cards retrieved successfully"));
    }

    @GetMapping("/paged")
    public ResponseEntity<ApiResponse<Page<PaymentCardDTO>>> getAllCards(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort) {

        log.info("Fetching paginated cards for user id: {}, page: {}, size: {}, sort: {}", userId, page, size, sort);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        Page<PaymentCardDTO> cardsPage = paymentCardService.getAllCardsByUserId(userId, pageable);

        return ResponseEntity.ok(ApiResponse.success(cardsPage, "Paginated cards retrieved successfully"));
    }

    @PutMapping("/{cardId}")
    public ResponseEntity<ApiResponse<PaymentCardDTO>> updateCard(
            @PathVariable Long userId,
            @PathVariable Long cardId,
            @Valid @RequestBody PaymentCardDTO paymentCardDTO) {
        
        log.info("Updating card with id: {} for user id: {}", cardId, userId);

        // Проверяем, что карта принадлежит пользователю
        checkCardOwnership(userId, cardId);
            
        PaymentCardDTO updatedCard = paymentCardService.updateCard(cardId, paymentCardDTO);
        return ResponseEntity.ok(ApiResponse.success(updatedCard, "Card updated successfully"));
    }

    @PatchMapping("/{cardId}/status")
    public ResponseEntity<ApiResponse<PaymentCardDTO>> updateCardStatus(
            @PathVariable Long userId,
            @PathVariable Long cardId,
            @RequestParam boolean active) {
        
        log.info("Updating card status - cardId: {}, active: {}", cardId, active);
        
        // Проверяем, что карта принадлежит пользователю
        checkCardOwnership(userId, cardId);
            
        PaymentCardDTO updatedCard = paymentCardService.updateCardStatus(cardId, active);
        String message = active ? "Card activated successfully" : "Card deactivated successfully";
        return ResponseEntity.ok(ApiResponse.success(updatedCard, message));
    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<ApiResponse<Void>> deleteCard(
            @PathVariable Long userId,
            @PathVariable Long cardId) {
        
        log.info("Deleting card with id: {} for user id: {}", cardId, userId);

        // Проверяем, что карта принадлежит пользователю
        checkCardOwnership(userId, cardId);

        paymentCardService.deleteCard(cardId);
        return ResponseEntity.noContent().build();
    }

    private PaymentCardDTO checkCardOwnership(Long userId, Long cardId) {
        PaymentCardDTO existingCard = paymentCardService.getCardById(cardId);
        if (!existingCard.getUserId().equals(userId)) {
            throw new EntityNotFoundException("Card not found for this user");
        }
        return existingCard;
    }
}