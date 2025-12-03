package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Data Transfer Object (DTO) для передачи данных о новом пользователе при регистрации.
 * Используется в API для валидации и передачи входных данных при создании пользователя.
 * Содержит обязательные поля: имя и email, которые проходят валидацию перед сохранением.
 * Особенности класса:
 * Использует Lombok {@link lombok.Data} для автоматической генерации:
 * геттеров и сеттеров
 * методов {@code equals()} и {@code hashCode()}
 * метода {@code toString()}
 * Поддерживает валидацию через аннотации Jakarta Validation
 * Предназначен исключительно для передачи данных (не содержит бизнес‑логики)
 *
 * @see jakarta.validation.constraints.NotNull
 * @see jakarta.validation.constraints.Email
 */

@Data
public class NewUser {
    /**
     * Имя пользователя.
     * Обязательное поле. Не может быть null или пустым.
     * Требования к значению:
     * не null
     * длина и формат не ограничены (на уровне DTO)
     */
    @NotNull(message = "Имя пользователя не может быть null")
    @NotEmpty(message = "Имя пользователя не может быть пустым")
    @NotBlank(message = "Имя пользователя не может содержать только пробелы")
    private String name;

    /**
     * Адрес электронной почты пользователя.
     * Обязательное поле. Должен соответствовать формату email и не может быть null.
     * Требования к значению:
     * не null
     * должен быть корректным email‑адресом (проходит валидацию)
     * обычно проверяется на уникальность на уровне сервиса
     * Пример корректного значения: {@code user@example.com}
     */
    @NotNull(message = "Email не может быть null")
    @Email(message = "Некорректный формат email-адреса")
    private String email;
}
