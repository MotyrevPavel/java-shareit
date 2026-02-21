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
        return BookingDto.builder()
                .id(booking.getId())
                .start(LocalDateTime.from(booking.getStart()))
                .end(LocalDateTime.from(booking.getEnd()))
                .status(booking.getStatus().name())
                .booker(UserMapper.toDto(booking.getBooker()))
                .item(ItemMapper.toDto(booking.getItem()))
                .build();
    }
}




