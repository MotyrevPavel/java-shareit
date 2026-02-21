package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) для создания нового запроса на аренду предмета.
 * <p>
 * Используется для:
 * <ul>
 *   <li>передачи данных от клиента при создании запроса на аренду;</li>
 *   <li>десериализации JSON из HTTP‑запросов (например, в методах POST /requests);</li>
 *   <li>валидации входных данных перед созданием сущности {@link ru.practicum.shareit.request.ItemRequest};</li>
 *   <li>изоляции входных данных от внутренней модели системы.</li>
 * </ul>
 *
 * <p>Отличается от {@link ItemRequestDto} и {@link ItemRequestWithAnswersDto} тем, что:
 * <ul>
 *   <li>содержит только поле {@code description} — минимально необходимые данные для создания запроса;</li>
 *   <li>не включает системные поля ({@code id}, {@code created}), которые генерируются на сервере;</li>
 *   <li>предназначена исключительно для операций создания (CREATE), а не для чтения или обновления.</li>
 * </ul>
 *
 * @see ItemRequestDto DTO с полной информацией о существующем запросе (включая ID и дату создания)
 * @see ItemRequestWithAnswersDto DTO с запросами и предложенными предметами
 * @see ru.practicum.shareit.request.ItemRequest сущность запроса на аренду в системе
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewItemRequest {

    /**
     * Описание запроса на аренду предмета от пользователя.
     *
     * <p>Содержит текст, в котором пользователь формулирует потребность в определённом предмете.
     * Служит основным источником информации для владельцев при просмотре запросов
     * и формировании предложений.
     *
     * <p>Требования к данным:
     * <ul>
     *   <li>не может быть {@code null} — обязательное поле для создания запроса;</li>
     *   <li>не должен быть пустой строкой или состоять только из пробелов;</li>
     * </ul>
     */
    private String description;
}
