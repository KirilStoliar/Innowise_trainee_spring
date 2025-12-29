package com.stoliar.controller;

import com.stoliar.dto.item.ItemCreateDto;
import com.stoliar.dto.item.ItemDto;
import com.stoliar.entity.Item;
import com.stoliar.exception.EntityNotFoundException;
import com.stoliar.mapper.ItemMapper;
import com.stoliar.repository.ItemRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
@Tag(name = "Item Management", description = "APIs for managing items")
public class ItemController {

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    @Operation(summary = "Create item", description = "Create a new item")
    @PostMapping
    public ResponseEntity<ItemDto> createItem(@Valid @RequestBody ItemCreateDto itemCreateDto) {
        Item item = itemMapper.toEntity(itemCreateDto);
        Item savedItem = itemRepository.save(item);
        ItemDto itemDto = itemMapper.toDto(savedItem);
        return ResponseEntity.status(HttpStatus.CREATED).body(itemDto);
    }

    @Operation(summary = "Get all items", description = "Get list of all items")
    @GetMapping
    public ResponseEntity<List<ItemDto>> getAllItems() {
        List<Item> items = itemRepository.findAll();
        List<ItemDto> itemDtos = itemMapper.toDtoList(items);
        return ResponseEntity.ok(itemDtos);
    }

    @Operation(summary = "Get item by ID", description = "Get item by its ID")
    @GetMapping("/{id}")
    public ResponseEntity<ItemDto> getItemById(@PathVariable Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item not found with id: " + id));
        ItemDto itemDto = itemMapper.toDto(item);
        return ResponseEntity.ok(itemDto);
    }
}