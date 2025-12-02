package com.stoliar.mapper;

import com.stoliar.dto.OrderItemDto;
import com.stoliar.entity.Item;
import com.stoliar.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    
    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "itemName", source = "item", qualifiedByName = "mapItemName")
    @Mapping(target = "itemPrice", source = "item", qualifiedByName = "mapItemPrice")
    OrderItemDto toDto(OrderItem orderItem);
    
    @Named("mapItemName")
    default String mapItemName(Item item) {
        return item != null ? item.getName() : null;
    }
    
    @Named("mapItemPrice")
    default Double mapItemPrice(Item item) {
        return item != null ? item.getPrice() : null;
    }
}