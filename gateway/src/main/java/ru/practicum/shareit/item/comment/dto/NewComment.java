package ru.practicum.shareit.item.comment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.validator.ConditionalNotBlank;

/**
 * DTO для создания нового комментария к предмету.
 * Представляет данные, передаваемые при отправке пользователем комментария
 * к определённому предмету в системе аренды. Используется исключительно
 * для операций создания (POST‑запросов), не содержит идентификатора
 * комментария или метаданных (автора, даты создания и т.д.),
 * которые обычно добавляются на стороне сервера.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewComment {

    /**
     * Текст комментария, оставляемого пользователем.
     * Требования к значению:
     * не может быть null — обязательное поле;
     * не может быть пустой строкой после обрезки пробелов
     * (проверка реализуется через аннотацию {@link ConditionalNotBlank});
     * содержимое может содержать любые символы, допустимые для строкового поля.
     * Валидация выполняется в два этапа:
     * Проверка на {@code null} с помощью {@link NotNull}.
     * Проверка на непустоту после обрезки с помощью {@link ConditionalNotBlank}.
     */
    @NotNull(message = "Текст комментария не может быть null")
    @ConditionalNotBlank
    private String text;
}

