package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithAnswersDto;
import ru.practicum.shareit.request.dto.NewItemRequest;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto create(Long userId, NewItemRequest newItemRequest);

    List<ItemRequestWithAnswersDto> getAllByUserId(Long userId);

    List<ItemRequestDto> getAllRequestsAnotherUsers(Long userId);

    ItemRequestWithAnswersDto findById(Long requestId);
}
