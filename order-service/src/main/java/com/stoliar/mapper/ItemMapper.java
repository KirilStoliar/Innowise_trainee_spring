package com.stoliar.mapper;

import com.stoliar.dto.item.ItemCreateDto;
import com.stoliar.dto.item.ItemDto;
import com.stoliar.dto.orderItem.OrderItemDto;
import com.stoliar.entity.Item;
import com.stoliar.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    // Маппинг OrderItem -> OrderItemDto
    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "itemName", source = "item", qualifiedByName = "mapItemName")
    @Mapping(target = "itemPrice", source = "item", qualifiedByName = "mapItemPrice")
    OrderItemDto toDto(OrderItem orderItem);

    // Маппинг Item -> ItemDto (для ответа с id)
    ItemDto toDto(Item item);

    // Маппинг списка Item -> List<ItemDto>
    List<ItemDto> toDtoList(List<Item> items);

    // Маппинг ItemCreateDto -> Item (для создания)
    Item toEntity(ItemCreateDto itemCreateDto);

    // Маппинг Item -> ItemCreateDto (для ответа при создании)
    ItemCreateDto toCreateDto(Item item);

    @Named("mapItemName")
    default String mapItemName(Item item) {
        return item != null ? item.getName() : null;
    }

    @Named("mapItemPrice")
    default Double mapItemPrice(Item item) {
        return item != null ? item.getPrice() : null;
    }
}