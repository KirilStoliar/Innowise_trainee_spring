package com.stoliar.mapper;

import com.stoliar.dto.order.OrderCreateDto;
import com.stoliar.dto.order.OrderResponseDto;
import com.stoliar.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ItemMapper.class})
public interface OrderMapper {
    
    @Mapping(target = "userEmail", source = "email")
    @Mapping(target = "userInfo", ignore = true)
    OrderResponseDto toResponseDto(Order order);
    
    List<OrderResponseDto> toResponseDtoList(List<Order> orders);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(target = "deleted", constant = "false")
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Order toEntity(OrderCreateDto orderCreateDto);
}