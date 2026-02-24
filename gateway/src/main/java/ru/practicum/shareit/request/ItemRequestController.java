package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.NewItemRequest;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> createRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                @Valid @RequestBody NewItemRequest newItemRequest) {
        log.info("Creating request {}, userId={}", newItemRequest, userId);
        return itemRequestClient.create(newItemRequest, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Get requests userId={}", userId);
        return itemRequestClient.getAllByUserId(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Get all requests except userId={}", userId);
        return itemRequestClient.getAllRequestsAnotherUsers(userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequest(@PathVariable Long requestId) {
        log.info("Get request requestId={}", requestId);
        return itemRequestClient.findById(requestId);
    }
}
