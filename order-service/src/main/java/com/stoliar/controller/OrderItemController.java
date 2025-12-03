package com.stoliar.controller;

import com.stoliar.dto.orderItem.OrderItemDto;
import com.stoliar.entity.Order;
import com.stoliar.entity.OrderItem;
import com.stoliar.exception.EntityNotFoundException;
import com.stoliar.mapper.ItemMapper;
import com.stoliar.repository.OrderItemRepository;
import com.stoliar.repository.OrderRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/order-items")
@RequiredArgsConstructor
@Tag(name = "Order Item Management", description = "APIs for managing order items")
public class OrderItemController {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final ItemMapper itemMapper;

    @Operation(summary = "Get all order items", description = "Get paginated list of all order items")
    @GetMapping
    public ResponseEntity<Page<OrderItemDto>> getAllOrderItems(
            @Parameter(description = "Page number (default: 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default: 10)") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field (default: createdAt)") @RequestParam(defaultValue = "createdAt") String sort) {
        
        log.info("Getting all order items - page: {}, size: {}, sort: {}", page, size, sort);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        Page<OrderItem> orderItemsPage = orderItemRepository.findAll(pageable);
        
        Page<OrderItemDto> orderItemDtos = orderItemsPage.map(itemMapper::toDto);
        return ResponseEntity.ok(orderItemDtos);
    }

    @Operation(summary = "Get order items for order", description = "Get all order items for a specific order")
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<OrderItemDto>> getOrderItemsByOrderId(
            @Parameter(description = "Order ID", required = true) @PathVariable Long orderId) {
        
        log.info("Getting order items for order id: {}", orderId);
        
        // Проверяем существование заказа
        orderRepository.findByIdAndNotDeleted(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));
        
        List<OrderItem> orderItems = orderItemRepository.findByOrderIdWithItems(orderId);
        List<OrderItemDto> orderItemDtos = orderItems.stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(orderItemDtos);
    }

    @Operation(summary = "Get order items by user ID", description = "Get all order items for orders of a specific user")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderItemDto>> getOrderItemsByUserId(
            @Parameter(description = "User ID", required = true) @PathVariable Long userId) {
        
        log.info("Getting order items for user id: {}", userId);
        
        // Получаем все заказы пользователя
        List<Order> userOrders = orderRepository.findAllByUserId(userId);
        
        // Получаем все OrderItem для этих заказов
        List<OrderItem> orderItems = userOrders.stream()
                .flatMap(order -> orderItemRepository.findByOrderIdWithItems(order.getId()).stream())
                .collect(Collectors.toList());
        
        List<OrderItemDto> orderItemDtos = orderItems.stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(orderItemDtos);
    }
}