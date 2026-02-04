package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

/**
 * Data Transfer Object (DTO) для передачи данных о предмете между слоями приложения.
 * Используется для:
 * ответа API при получении информации о предмете;
 * передачи данных между контроллером, сервисом и другими компонентами;
 * сериализации в JSON при HTTP‑ответах.
 * Отличается от сущности {@link Item} тем, что:
 * не содержит бизнес‑логики;
 * служит исключительно для транспортировки данных;
 * может быть адаптирован под конкретные API‑ответы (например, исключать поля).
 */

@Data
@Builder
public class ItemFullDto {

    /**
     * Уникальный идентификатор предмета.
     */
    private Long id;

    /**
     * Название предмета.
     * Должно быть информативным (например, "Дрель").
     */
    private String name;

    /**
     * Подробное описание предмета.
     * Может включать детали о состоянии, особенностях, комплектации.
     */
    private String description;

    /**
     * Статус доступности предмета для аренды.
     * true — предмет доступен для бронирования;
     * false — предмет временно не доступен;
     * Значение задаётся владельцем предмета.
     */
    private Boolean available;

    /**
     * Идентификатор пользователя‑владельца предмета.
     * Связывает предмет с конкретным пользователем в системе.
     */
    private Long userId;

    private BookingDto nextBooking;

    private BookingDto lastBooking;

    private List<CommentDto> comments;
}
