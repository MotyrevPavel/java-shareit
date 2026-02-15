package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NewItemRequest {
    @NotNull(message = "Имя вещи не может быть null")
    @NotEmpty(message = "Имя вещи не может быть пустым")
    @NotBlank(message = "Имя вещи не может содержать только пробелы")
    String description;
}
