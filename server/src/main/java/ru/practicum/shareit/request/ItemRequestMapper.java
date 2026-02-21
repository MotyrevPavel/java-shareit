package ru.practicum.shareit.request;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithAnswersDto;
import ru.practicum.shareit.request.dto.NewItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@UtilityClass
public class ItemRequestMapper {
    public static ItemRequest toItemRequest(NewItemRequest newItemRequest, User user) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription(newItemRequest.getDescription());
        itemRequest.setUser(user);
        itemRequest.setCreated(LocalDateTime.now());
        return itemRequest;
    }

    public static ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        return ItemRequestDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreated())
                .build();
    }

    public static ItemRequestWithAnswersDto toItemRequestDtoWithAnswers(ItemRequest itemRequest) {
        List<ItemShortDto> itemShortDtoList;
        if (itemRequest.getItems() != null) {
            itemShortDtoList = itemRequest.getItems().stream()
                    .map(ItemMapper::itemShortDto)
                    .toList();
        } else {
            itemShortDtoList = Collections.emptyList();
        }
        return ItemRequestWithAnswersDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreated())
                .items(itemShortDtoList)
                .build();
    }
}
