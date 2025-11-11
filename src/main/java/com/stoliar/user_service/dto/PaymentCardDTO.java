package com.stoliar.user_service.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PaymentCardDTO {
    private Long id;
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotBlank(message = "Card number is required")
    @Size(min = 16, max = 19, message = "Card number must be between 16 and 19 characters")
    private String number;
    
    @NotBlank(message = "Card holder is required")
    private String holder;
    
    @NotNull(message = "Expiration date is required")
    @Future(message = "Expiration date must be in the future")
    private LocalDate expirationDate;
    
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}