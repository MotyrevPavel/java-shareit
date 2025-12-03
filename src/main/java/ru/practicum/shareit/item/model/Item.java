package ru.practicum.shareit.item.model;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemService;

/**
 * Модель предмета (вещи) в системе аренды.
 * Представляет сущность "предмет", который пользователь может:
 * добавить в каталог;
 * предоставить в аренду;
 * управлять его доступностью
 * Основные характеристики предмета:
 * уникальный идентификатор ({@link #id});
 * название ({@link #name});
 * описание ({@link #description});
 * статус доступности для аренды ({@link #available});
 * идентификатор владельца ({@link #userId}).
 *
 * @see ItemController
 * @see ItemService
 * @see ItemRepository
 */

@Data
@Builder
public class Item {

    /**
     * Уникальный идентификатор предмета.
     * Может быть null при создании (будет назначен БД).
     */
    Long id;

    /**
     * Название предмета.
     * Обязательное поле, должно отражать суть вещи (например, "Дрель").
     */
    String name;

    /**
     * Подробное описание предмета.
     * Может содержать детали о состоянии, особенностях, комплектации и т.п.
     */
    String description;

    /**
     * Статус доступности предмета для аренды.
     * true — предмет доступен для бронирования;
     * false — предмет временно не доступен;
     * Значение устанавливает владелец предмета.
     */
    Boolean available;

    /**
     * Идентификатор пользователя-владельца предмета.
     * Связывает предмет с конкретным пользователем системы.
     */
    Long userId;
}
