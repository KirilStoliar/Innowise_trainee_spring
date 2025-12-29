package com.stoliar.dto;

import com.stoliar.entity.Order;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderUpdateDto {
    @NotNull(message = "Status is required")
    private Order.OrderStatus status;
}