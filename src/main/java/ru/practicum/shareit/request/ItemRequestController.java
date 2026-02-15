package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithAnswersDto;
import ru.practicum.shareit.request.dto.NewItemRequest;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ResponseEntity<ItemRequestDto> createRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                        @Valid @RequestBody NewItemRequest newItemRequest) {
        return ResponseEntity.ok().body(itemRequestService.create(userId, newItemRequest));
    }

    @GetMapping
    public ResponseEntity<List<ItemRequestWithAnswersDto>> getRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return ResponseEntity.ok().body(itemRequestService.getAllByUserId(userId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ItemRequestDto>> getAllRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return ResponseEntity.ok().body(itemRequestService.getAllRequestsAnotherUsers(userId));
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<ItemRequestWithAnswersDto> getRequest(@PathVariable Long requestId) {
        return ResponseEntity.ok().body(itemRequestService.findById(requestId));
    }
}
