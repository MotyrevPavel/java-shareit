package ru.practicum.shareit.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import ru.practicum.shareit.booking.dto.NewBooking;

import java.time.LocalDateTime;

public class BookingDateCorrectValidator implements ConstraintValidator<BookingDateCorrect, NewBooking> {

    @Override
    public void initialize(BookingDateCorrect constraintAnnotation) {
    }

    @Override
    public boolean isValid(NewBooking booking, ConstraintValidatorContext context) {
        LocalDateTime start = booking.getStart();
        LocalDateTime end = booking.getEnd();
        if (start == null || end == null) {
            return false;
        }
        if (start.isBefore(LocalDateTime.now())) {
            return false;
        }

        return start.isBefore(end);
    }
}