package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comment.dto.NewComment;
import ru.practicum.shareit.item.dto.NewItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> create(@Valid @RequestBody NewItemDto itemDto,
                                         @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Creating item {}, userId={}", itemDto, userId);
        return itemClient.create(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@Valid @RequestBody UpdateItemDto itemDto,
                                         @PathVariable Long itemId,
                                         @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Updating item {}, itemId={}, userId={}", itemDto, itemId, userId);
        return itemClient.update(itemDto, itemId, userId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getById(@PathVariable Long itemId) {
        log.info("Get itemId={}", itemId);
        return itemClient.getById(itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getByUserId(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Get items userId={}", userId);
        return itemClient.getByItemsByUserId(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestParam String text) {
        log.info("Get items byParam={}", text);
        return itemClient.searchAvailableItemByParam(text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> create(@PathVariable Long itemId,
                                         @RequestHeader("X-Sharer-User-Id") Long userId,
                                         @Valid @RequestBody NewComment comment) {
        return itemClient.createNewComment(itemId, userId, comment);
    }
}
