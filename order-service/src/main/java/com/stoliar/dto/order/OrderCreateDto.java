package com.stoliar.dto.order;

import com.stoliar.dto.orderItem.OrderItemCreateDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrderCreateDto {
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotEmpty(message = "Order must contain at least one item")
    private List<@Valid OrderItemCreateDto> orderItems;
}