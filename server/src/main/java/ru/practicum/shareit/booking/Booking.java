package ru.practicum.shareit.booking;

import jakarta.persistence.*;
import lombok.Data;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

/**
 * Сущность бронирования предмета в системе аренды.
 * Представляет запись о бронировании в базе данных, включая информацию
 * о периоде аренды, предмете, арендаторе и статусе бронирования.
 * Соответствует таблице bookings в БД.
 * Основные связи:
 * связан с сущностью {@link Item} (предмет, который забронирован);
 * связан с сущностью {@link User} (пользователь, который создал бронирование).
 * Особенности реализации:
 * использует автоинкрементный идентификатор ({@link GenerationType#IDENTITY});
 * даты хранятся как {@code TIMESTAMP} в БД;
 * статус бронирования сохраняется как строка ({@link EnumType#STRING}) в БД.
 *
 * @see Item сущность предмета, который бронируется
 * @see User сущность пользователя (арендатора)
 * @see BookingState перечисление возможных статусов бронирования
 */

@Data
@Entity
@Table(name = "bookings")
public class Booking {

    /**
     * Уникальный идентификатор бронирования в системе.
     * Автоматически генерируется БД при сохранении новой записи
     * (стратегия {@link GenerationType#IDENTITY}).
     * Особенности:
     * первичный ключ таблицы "bookings";
     * не может быть null после сохранения в БД;
     * используется для однозначной идентификации бронирования.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Дата и время начала периода бронирования.
     * Указывает, когда арендатор планирует начать использование предмета.
     * Соответствует колонке "start" в таблице "bookings".
     * Особенности хранения в БД:
     * тип данных: {@code TIMESTAMP};
     * не может быть {@code null};
     * формат: стандартный для {@link LocalDateTime}.
     */
    @Column(name = "start", columnDefinition = "TIMESTAMP", nullable = false)
    private LocalDateTime start;

    /**
     * Дата и время окончания периода бронирования.
     * Указывает, когда арендатор планирует вернуть предмет.
     * Соответствует колонке "finish" в таблице "bookings".
     * Особенности хранения в БД:
     * тип данных: "TIMESTAMP";
     * не может быть "null";
     * должна быть позже "start".
     */
    @Column(name = "finish", columnDefinition = "TIMESTAMP", nullable = false)
    private LocalDateTime end;

    /**
     * Предмет, который забронирован.
     * Ссылка на сущность {@link Item}, представляющую предмет в каталоге.
     * Связана с колонкой {@code item_id} в таблице {@code bookings}.
     * Особенности связи:
     * связь «многие‑к‑одному» ({@link ManyToOne});
     * загрузка по требованию ({@link FetchType#LAZY});
     * не может быть {@code null} — каждое бронирование относится к конкретному предмету.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    /**
     * Пользователь (арендатор), который создал бронирование.
     * Ссылка на сущность {@link User}, представляющую пользователя системы.
     * Связана с колонкой {@code user_id} в таблице {@code bookings}.
     * Особенности связи:
     * связь «многие‑к‑одному» ({@link ManyToOne});
     * загрузка по требованию ({@link FetchType#LAZY});
     * не может быть {@code null} — каждое бронирование создано конкретным пользователем.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User booker;

    /**
     * Статус бронирования в системе.
     * Отражает этап жизненного цикла бронирования. Хранится в БД как строка
     * ({@link EnumType#STRING}). Возможные значения определяются
     * перечислением {@link BookingState}.
     * Примеры статусов:
     * {@code WAITING} — ожидает подтверждения;
     * {@code APPROVED} — подтверждено;
     * {@code REJECTED} — отклонено;
     * {@code COMPLETED} — завершено.
     */
    @Enumerated(EnumType.STRING)
    private BookingState status;
}
