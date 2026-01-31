package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.*;


import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemDto> create(@Valid @RequestBody NewItem newItem,
                                          @RequestHeader("X-Sharer-User-Id") Long userId) {
        ItemDto itemDto = itemService.create(newItem, userId);
        return ResponseEntity.ok().body(itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemDto> update(@Valid @RequestBody UpdateItem updateItem,
                                          @PathVariable Long itemId,
                                          @RequestHeader("X-Sharer-User-Id") Long userId) {
        ItemDto itemDto = itemService.update(updateItem, itemId, userId);
        return ResponseEntity.ok().body(itemDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDto> getById(@PathVariable Long itemId) {
        ItemDto itemDto = itemService.getById(itemId);
        return ResponseEntity.ok().body(itemDto);
    }

    @GetMapping
    public ResponseEntity<List<ItemDto>> getByUserId(@RequestHeader("X-Sharer-User-Id") Long userId) {
        List<ItemDto> items = itemService.getAllByUserId(userId);
        return ResponseEntity.ok().body(items);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> search(@RequestParam String text) {
        List<ItemDto> items = itemService.searchAvailableItemByParam(text);
        return ResponseEntity.ok().body(items);
    }
}
