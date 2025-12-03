package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.model.Item;

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
 *
 * @see Item
 * @see ItemController
 * @see ItemService
 */

@Data
@Builder
public class ItemDto {

    /**
     * Уникальный идентификатор предмета.
     */
    Long id;

    /**
     * Название предмета.
     * Должно быть информативным (например, "Дрель").
     */
    String name;

    /**
     * Подробное описание предмета.
     * Может включать детали о состоянии, особенностях, комплектации.
     */
    String description;

    /**
     * Статус доступности предмета для аренды.
     * true — предмет доступен для бронирования;
     * false — предмет временно не доступен;
     * Значение задаётся владельцем предмета.
     */
    Boolean available;

    /**
     * Идентификатор пользователя‑владельца предмета.
     * Связывает предмет с конкретным пользователем в системе.
     */
    Long userId;
}
