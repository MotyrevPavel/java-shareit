package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBooking;

import java.util.List;

public interface BookingService {
    BookingDto create(NewBooking newBooking, Long bookerId);

    BookingDto approve(Long bookingId, Boolean approved, Long ownerId);

    BookingDto getById(Long bookingId, Long userId);

    List<BookingDto> getAllBookingsByBooker(Long bookerId, BookingState state);

    List<BookingDto> getAllBookingsByOwner(Long ownerId, BookingState state);
}