package ru.practicum.shareit.item.model;

import jakarta.persistence.*;
import lombok.Data;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.model.User;

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
 * владелец предмета ({@link #user}).
 */

@Data
@Entity
@Table(name = "items")
public class Item {

    /**
     * Уникальный идентификатор предмета.
     * Может быть null при создании (будет назначен БД).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Название предмета.
     * Обязательное поле, должно отражать суть вещи (например, "Дрель").
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Подробное описание предмета.
     * Может содержать детали о состоянии, особенностях, комплектации и т.п.
     */
    @Column(name = "description")
    private String description;

    /**
     * Статус доступности предмета для аренды.
     * true — предмет доступен для бронирования;
     * false — предмет временно не доступен;
     * Значение устанавливает владелец предмета.
     */
    @Column(name = "available", nullable = false)
    private Boolean available;

    /**
     * Идентификатор пользователя-владельца предмета.
     * Связывает предмет с конкретным пользователем системы.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "request_id")
    private ItemRequest request;
}
