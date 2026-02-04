package ru.practicum.shareit.booking;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBooking;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@UtilityClass
public class BookingMapper {
    public Booking toBooking(NewBooking newBooking, Item item, User booker) {
        Booking booking = new Booking();
        booking.setStart(newBooking.getStart());
        booking.setEnd(newBooking.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingState.WAITING);
        return booking;
    }

    public BookingDto toBookingDto(Booking booking) {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(booking.getId());
        bookingDto.setStart(LocalDateTime.from(booking.getStart()));
        bookingDto.setEnd(LocalDateTime.from(booking.getEnd()));
        bookingDto.setStatus(booking.getStatus().name());
        bookingDto.setBooker(UserMapper.toDto(booking.getBooker()));
        bookingDto.setItem(ItemMapper.toDto(booking.getItem()));
        return bookingDto;
    }
}




