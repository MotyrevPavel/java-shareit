package ru.practicum.shareit.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE})  // Применяется к классу!
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = BookingDateCorrectValidator.class)
public @interface BookingDateCorrect {
    String message() default "Даты указаны некорректно";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
