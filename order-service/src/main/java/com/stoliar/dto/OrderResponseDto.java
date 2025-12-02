package com.stoliar.dto;

import com.stoliar.entity.Order;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponseDto {
    private Long id;
    private Long userId;
    private String userEmail;
    private Order.OrderStatus status;
    private Double totalPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemDto> orderItems;
    private UserInfoDto userInfo; // Информация из User Service
}