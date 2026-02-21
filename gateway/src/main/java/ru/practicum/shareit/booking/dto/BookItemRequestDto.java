package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO для запроса на создание бронирования предмета.
 * Представляет данные, необходимые для оформления бронирования конкретного предмета
 * (например, инструмента или оборудования) в системе аренды.
 * Основные правила валидации:
 * дата начала не может быть в прошлом;
 * дата окончания должна быть строго в будущем;
 * обе даты должны быть указаны.
 *
 * @see LocalDateTime для формата хранения дат
 * @see @FutureOrPresent для валидации даты начала
 * @see @Future для валидации даты окончания
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookItemRequestDto {

    /**
     * Идентификатор предмета, который пользователь хочет забронировать.
     * Должен соответствовать существующему предмету в системе.
     */
    private long itemId;

    /**
     * Дата и время начала периода бронирования.
     * Требования к значению:
     * не может быть null;
     * должна быть в настоящем или будущем (не в прошлом);
     * должна быть раньше даты окончания бронирования.
     * Формат сериализации JSON: "yyyy-MM-dd'T'HH:mm:ss"
     */
    @FutureOrPresent
    @NotNull(message = "Дата начала бронирования должна быть указана")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime start;

    /**
     * Дата и время окончания периода бронирования.
     * Требования к значению:
     * не может быть null;
     * должна быть строго в будущем (не может совпадать с настоящим моментом);
     * Формат сериализации JSON: "yyyy-MM-dd'T'HH:mm:ss"
     */
    @Future
    @NotNull(message = "Дата окончания бронирования должна быть указана")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime end;
}