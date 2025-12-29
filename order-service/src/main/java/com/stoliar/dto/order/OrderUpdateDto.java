package com.stoliar.dto.order;

import com.stoliar.dto.orderItem.OrderItemUpdateDto;
import com.stoliar.entity.Order;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrderUpdateDto {
    @NotNull(message = "Status is required")
    @Schema(
            description = "Order status",
            example = "PENDING",
            allowableValues = {"PENDING", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED"}
    )
    private Order.OrderStatus status;

    @Schema(description = "User ID (optional, if you want to change user)")
    private Long userId;

    @Schema(description = "List of order items to update")
    private List<@Valid OrderItemUpdateDto> orderItems;
}