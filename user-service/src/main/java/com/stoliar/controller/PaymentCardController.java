package com.stoliar.controller;

import com.stoliar.dto.PaymentCardCreateDTO;
import com.stoliar.dto.PaymentCardDTO;
import com.stoliar.exception.EntityNotFoundException;
import com.stoliar.response.ApiResponse;
import com.stoliar.service.PaymentCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/{userId}/payment-cards")
@Validated
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Payment Card Management", description = "APIs for managing user payment cards")
public class PaymentCardController {

    private final PaymentCardService paymentCardService;

    @Operation(summary = "Create payment card", description = "Create a new payment card for user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Payment card created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal")
    public ResponseEntity<ApiResponse<PaymentCardDTO>> createPaymentCard(
            @Parameter(description = "User ID", required = true) @PathVariable Long userId,
            @Valid @RequestBody PaymentCardCreateDTO paymentCardCreateDTO) {

        log.info("Creating payment card for user id: {}", userId);

        PaymentCardDTO createdCard = paymentCardService.createPaymentCard(userId, paymentCardCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdCard, "Payment card created successfully"));
    }

    @Operation(summary = "Get payment card by ID", description = "Retrieve a specific payment card for user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment card retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Card or user not found")
    })
    @GetMapping("/{cardId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal")
    public ResponseEntity<ApiResponse<PaymentCardDTO>> getCardById(
            @Parameter(description = "User ID", required = true) @PathVariable Long userId,
            @Parameter(description = "Payment Card ID", required = true) @PathVariable Long cardId) {

        log.info("Fetching card by id: {} for user id: {}", cardId, userId);

        PaymentCardDTO card = checkCardOwnership(userId, cardId);
        return ResponseEntity.ok(ApiResponse.success(card, "Card retrieved successfully"));
    }

    @Operation(summary = "Get all payment cards", description = "Retrieve all payment cards for user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment cards retrieved successfully")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal")
    public ResponseEntity<ApiResponse<List<PaymentCardDTO>>> getAllCardsByUserId(
            @Parameter(description = "User ID", required = true) @PathVariable Long userId) {

        log.info("Fetching all cards for user id: {}", userId);

        List<PaymentCardDTO> cards = paymentCardService.getAllCardsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(cards, "Cards retrieved successfully"));
    }

    @Operation(summary = "Get paginated payment cards", description = "Retrieve paginated payment cards for user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Paginated payment cards retrieved successfully")
    })
    @GetMapping("/paged")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal")
    public ResponseEntity<ApiResponse<Page<PaymentCardDTO>>> getAllCards(
            @Parameter(description = "User ID", required = true) @PathVariable Long userId,
            @Parameter(description = "Page number (default: 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default: 10)") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field (default: createdAt)") @RequestParam(defaultValue = "createdAt") String sort) {

        log.info("Fetching paginated cards for user id: {}, page: {}, size: {}, sort: {}", userId, page, size, sort);

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        Page<PaymentCardDTO> cardsPage = paymentCardService.getAllCardsByUserId(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(cardsPage, "Paginated cards retrieved successfully"));
    }

    @Operation(summary = "Update payment card", description = "Update an existing payment card")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment card updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Card or user not found")
    })
    @PutMapping("/{cardId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal")
    public ResponseEntity<ApiResponse<PaymentCardDTO>> updateCard(
            @Parameter(description = "User ID", required = true) @PathVariable Long userId,
            @Parameter(description = "Payment Card ID", required = true) @PathVariable Long cardId,
            @Valid @RequestBody PaymentCardDTO paymentCardDTO) {

        log.info("Updating card with id: {} for user id: {}", cardId, userId);

        checkCardOwnership(userId, cardId);
        PaymentCardDTO updatedCard = paymentCardService.updateCard(cardId, paymentCardDTO);
        return ResponseEntity.ok(ApiResponse.success(updatedCard, "Card updated successfully"));
    }

    @Operation(summary = "Update payment card status", description = "Activate or deactivate a payment card")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment card status updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Card or user not found")
    })
    @PatchMapping("/{cardId}/status")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal")
    public ResponseEntity<ApiResponse<PaymentCardDTO>> updateCardStatus(
            @Parameter(description = "User ID", required = true) @PathVariable Long userId,
            @Parameter(description = "Payment Card ID", required = true) @PathVariable Long cardId,
            @Parameter(description = "Activation status", required = true) @RequestParam boolean active) {

        log.info("Updating card status - cardId: {}, active: {}", cardId, active);

        checkCardOwnership(userId, cardId);
        PaymentCardDTO updatedCard = paymentCardService.updateCardStatus(cardId, active);
        String message = active ? "Card activated successfully" : "Card deactivated successfully";
        return ResponseEntity.ok(ApiResponse.success(updatedCard, message));
    }

    @Operation(summary = "Delete payment card", description = "Delete a payment card")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Payment card deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Card or user not found")
    })
    @DeleteMapping("/{cardId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal")
    public ResponseEntity<ApiResponse<Void>> deleteCard(
            @Parameter(description = "User ID", required = true) @PathVariable Long userId,
            @Parameter(description = "Payment Card ID", required = true) @PathVariable Long cardId) {

        log.info("Deleting card with id: {} for user id: {}", cardId, userId);

        paymentCardService.deleteCard(userId, cardId);
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