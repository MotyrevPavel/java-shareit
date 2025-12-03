package ru.practicum.shareit.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ConditionalNotBlankValidator.class)
public @interface ConditionalNotBlank {
    String message() default "Если поле указано, оно не может быть пустым или содержать только пробелы";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}