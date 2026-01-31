package ru.practicum.shareit.item;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.dto.*;

@UtilityClass
public class ItemMapper {

    public static ItemDto toDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .userId(item.getUserId())
                .build();

    }

    public static Item toItem(NewItem newItem, Long userId) {
        return Item.builder()
                .name(newItem.getName())
                .description(newItem.getDescription())
                .available(newItem.getAvailable())
                .userId(userId)
                .build();
    }

    public static Item toItem(Item item, UpdateItem updateItem) {

        if (updateItem.hasName()) {
            item.setName(updateItem.getName());
        }

        if (updateItem.hasDescription()) {
            item.setDescription(updateItem.getDescription());
        }

        if (updateItem.hasAvailable()) {
            item.setAvailable(updateItem.getAvailable());
        }

        return item;
    }
}