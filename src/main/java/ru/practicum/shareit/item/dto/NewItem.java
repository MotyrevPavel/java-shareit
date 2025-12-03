package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.model.Item;

/**
 * Модель данных для создания нового предмета в системе аренды.
 * Используется при:
 * отправке HTTP‑запроса на добавление предмета;
 * валидации входных данных перед сохранением;
 * передаче данных из контроллера в сервисный слой.
 * Особенности:
 * все поля обязательны для заполнения;
 * строковые поля проходят многоуровневую проверку (null, пусто, пробелы);
 * поле {@link #available} требует явного указания true/false (не допускает null).
 *
 * @see Item
 * @see ItemController
 * @see ItemService
 */

@Data
public class NewItem {

    /**
     * Название предмета.
     * Требования к значению:
     * не может быть null ({@link NotNull});
     * не может быть пустой строкой ({@link NotEmpty});
     * не может состоять только из пробелов ({@link NotBlank}).
     * Пример: "Дрель", "Велосипед горный".
     */
    @NotNull(message = "Имя вещи не может быть null")
    @NotEmpty(message = "Имя вещи не может быть пустым")
    @NotBlank(message = "Имя вещи не может содержать только пробелы")
    String name;

    /**
     * Подробное описание предмета.
     * Требования к значению (аналогично полю {@link #name}):
     * не может быть null;
     * не может быть пустой строкой;
     * не может состоять только из пробелов.
     * Пример: "Стул из массива дуба, выдерживает нагрузку до 120 кг".
     */
    @NotNull(message = "Описание не может быть null")
    @NotEmpty(message = "Описание не может быть пустым")
    @NotBlank(message = "Описание не может содержать только пробелы")
    String description;

    /**
     * Статус доступности предмета для аренды.
     * Требования:
     * обязательно указать значение ({@link NotNull});
     * допустимы только {@code true} (доступно) или {@code false} (недоступно).
     * Значение устанавливает владелец предмета при создании записи.
     * Важно: null не допускается — необходимо явно указать статус.
     */
    @NotNull(message = "Доступность товара должна быть указана (true/false)")
    Boolean available;
}