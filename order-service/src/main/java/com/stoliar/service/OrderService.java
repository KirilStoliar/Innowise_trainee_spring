package com.stoliar.service;

import com.stoliar.client.UserServiceClient;
import com.stoliar.dto.*;
import com.stoliar.entity.Order;
import com.stoliar.entity.OrderItem;
import com.stoliar.entity.Item;
import com.stoliar.mapper.OrderMapper;
import com.stoliar.mapper.ItemMapper;
import com.stoliar.repository.OrderRepository;
import com.stoliar.repository.OrderItemRepository;
import com.stoliar.repository.ItemRepository;
import com.stoliar.specification.OrderSpecification;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ItemRepository itemRepository;
    private final OrderSpecification orderSpecification;
    private final OrderMapper orderMapper;
    private final ItemMapper itemMapper;
    private final UserServiceClient userServiceClient;

    @Transactional
    public OrderResponseDto createOrder(@Valid OrderCreateDto orderCreateDto) {
        log.info("Creating order for user id: {}", orderCreateDto.getUserId());

        UserInfoDto userInfo = userServiceClient.getUserById(orderCreateDto.getUserId());

        Order order = new Order(); // Создаем сущность напрямую вместо маппера
        order.setUserId(orderCreateDto.getUserId());
        order.setEmail(userInfo.getEmail());
        order.setStatus(Order.OrderStatus.PENDING);
        order.setDeleted(false);

        createOrderItems(order, orderCreateDto.getOrderItems());
        calculateTotalPrice(order);

        Order savedOrder = orderRepository.save(order);
        log.info("Order created with id: {}", savedOrder.getId());

        return enrichOrderWithUserInfo(savedOrder, userInfo);
    }

    @Transactional
    public OrderResponseDto getOrderById(Long id) {
        log.info("Getting order by id: {}", id);

        Order order = orderRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        UserInfoDto userInfo = userServiceClient.getUserById(order.getUserId());

        return enrichOrderWithUserInfo(order, userInfo);
    }

    @Transactional
    public Page<OrderResponseDto> getOrdersWithFilters(@Valid OrderFilterDto filterDto) {
        log.info("Getting orders with filters");

        Pageable pageable = PageRequest.of(filterDto.getPage(), filterDto.getSize());
        Specification<Order> spec = orderSpecification.withFilters(
                filterDto.getCreatedFrom(), filterDto.getCreatedTo(), filterDto.getStatuses());

        return orderRepository.findAll(spec, pageable)
                .map(order -> enrichOrderWithUserInfo(order, userServiceClient.getUserById(order.getUserId())));
    }

    public Page<OrderResponseDto> getOrdersByUserId(Long userId, Pageable pageable) {
        log.info("Getting orders for user: {}", userId);

        UserInfoDto userInfo = userServiceClient.getUserById(userId);

        return orderRepository.findByUserId(userId, pageable)
                .map(order -> enrichOrderWithUserInfo(order, userInfo));
    }

    @Transactional
    public OrderResponseDto updateOrder(Long id, @Valid OrderUpdateDto orderUpdateDto) {
        log.info("Updating order with id: {}", id);

        Order existingOrder = orderRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        existingOrder.setStatus(orderUpdateDto.getStatus());
        Order updatedOrder = orderRepository.save(existingOrder);

        UserInfoDto userInfo = userServiceClient.getUserById(updatedOrder.getUserId());

        return enrichOrderWithUserInfo(updatedOrder, userInfo);
    }

    @Transactional
    public void deleteOrder(Long id) {
        log.info("Deleting order with id: {}", id);

        if (!orderRepository.existsByIdAndNotDeleted(id)) {
            throw new RuntimeException("Order not found with id: " + id);
        }

        orderItemRepository.deleteByOrderId(id);
        orderRepository.softDeleteById(id);
    }

    // Вспомогательные методы
    private void createOrderItems(Order order, List<OrderItemDto> orderItemDtos) {
        if (orderItemDtos == null || orderItemDtos.isEmpty()) {
            throw new RuntimeException("Order must contain at least one item");
        }

        List<OrderItem> orderItems = orderItemDtos.stream()
                .map(dto -> {
                    Item item = itemRepository.findById(dto.getItemId())
                            .orElseThrow(() -> new RuntimeException("Item not found with id: " + dto.getItemId()));

                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(order);
                    orderItem.setItem(item);
                    orderItem.setQuantity(dto.getQuantity());
                    return orderItem;
                })
                .collect(Collectors.toList());

        order.setOrderItems(orderItems);
    }

    private void calculateTotalPrice(Order order) {
        double total = order.getOrderItems().stream()
                .mapToDouble(item -> item.getItem().getPrice() * item.getQuantity())
                .sum();

        order.setTotalPrice(total);
    }

    private OrderResponseDto enrichOrderWithUserInfo(Order order, UserInfoDto userInfo) {
        OrderResponseDto responseDto = orderMapper.toResponseDto(order);
        responseDto.setUserInfo(userInfo);

        if (order.getOrderItems() != null) {
            List<OrderItemDto> enrichedItems = order.getOrderItems().stream()
                    .map(itemMapper::toDto)
                    .collect(Collectors.toList());
            responseDto.setOrderItems(enrichedItems);
        }

        return responseDto;
    }
}