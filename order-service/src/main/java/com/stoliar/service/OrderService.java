package com.stoliar.service;

import com.stoliar.dto.OrderCreateDto;
import com.stoliar.dto.OrderFilterDto;
import com.stoliar.dto.OrderResponseDto;
import com.stoliar.dto.OrderUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    
    OrderResponseDto createOrder(OrderCreateDto orderCreateDto);
    OrderResponseDto getOrderById(Long id);
    Page<OrderResponseDto> getOrdersWithFilters(OrderFilterDto filterDto);
    Page<OrderResponseDto> getOrdersByUserId(Long userId, Pageable pageable);
    OrderResponseDto updateOrder(Long id, OrderUpdateDto orderUpdateDto);
    void deleteOrder(Long id);
}