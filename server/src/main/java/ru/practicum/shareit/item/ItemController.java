package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.dto.NewComment;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemFullDto;
import ru.practicum.shareit.item.dto.NewItem;
import ru.practicum.shareit.item.dto.UpdateItem;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemDto> create(@RequestBody NewItem newItem,
                                          @RequestHeader("X-Sharer-User-Id") Long userId) {
        ItemDto itemDto = itemService.create(newItem, userId);
        return ResponseEntity.ok().body(itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemDto> update(@RequestBody UpdateItem updateItem,
                                          @PathVariable Long itemId,
                                          @RequestHeader("X-Sharer-User-Id") Long userId) {
        ItemDto itemDto = itemService.update(updateItem, itemId, userId);
        return ResponseEntity.ok().body(itemDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemFullDto> getById(@PathVariable Long itemId) {
        ItemFullDto itemDto = itemService.getById(itemId);
        return ResponseEntity.ok().body(itemDto);
    }

    @GetMapping
    public ResponseEntity<List<ItemFullDto>> getByUserId(@RequestHeader("X-Sharer-User-Id") Long userId) {
        List<ItemFullDto> items = itemService.getAllByUserId(userId);
        return ResponseEntity.ok().body(items);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> search(@RequestParam String text) {
        List<ItemDto> items = itemService.searchAvailableItemByParam(text);
        return ResponseEntity.ok().body(items);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<CommentDto> create(@PathVariable Long itemId,
                                             @RequestHeader("X-Sharer-User-Id") Long userId,
                                             @RequestBody NewComment comment) {
        CommentDto commentDto = itemService.createNewComment(itemId, userId, comment);
        return ResponseEntity.ok().body(commentDto);
    }
}
