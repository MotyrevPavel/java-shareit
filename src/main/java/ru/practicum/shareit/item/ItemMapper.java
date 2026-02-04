package ru.practicum.shareit.item;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@UtilityClass
public class ItemMapper {

    public static ItemDto toDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .userId(item.getUser().getId())
                .build();
    }

    public static ItemFullDto toItemFullDto(Item item, BookingDto nextBooking,
                                            BookingDto lastBooking, List<CommentDto> comments) {
        return ItemFullDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .userId(item.getUser().getId())
                .comments(comments)
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .build();
    }

    public static Item toItem(NewItem newItem, User user) {
        Item item = new Item();
        item.setName(newItem.getName());
        item.setDescription(newItem.getDescription());
        item.setAvailable(newItem.getAvailable());
        item.setUser(user);
        return item;
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