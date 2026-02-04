package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBooking;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService service;

    @PostMapping
    public ResponseEntity<BookingDto> createBooking(@Valid @RequestBody NewBooking newBooking,
                                                    @RequestHeader("X-Sharer-User-Id") Long bookerId) {
        BookingDto bookingDto = service.create(newBooking, bookerId);
        return ResponseEntity.ok().body(bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingDto> approveBooking(@PathVariable Long bookingId,
                                                     @RequestParam Boolean approved,
                                                     @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        BookingDto bookingDto = service.approve(bookingId, approved, ownerId);
        return ResponseEntity.ok().body(bookingDto);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDto> getBookingById(@PathVariable Long bookingId,
                                                     @RequestHeader("X-Sharer-User-Id") Long userId) {
        BookingDto bookingDto = service.getById(bookingId, userId);
        return ResponseEntity.ok().body(bookingDto);
    }

    @GetMapping()
    public ResponseEntity<List<BookingDto>> getBookingByBooker(@RequestHeader("X-Sharer-User-Id") Long bookerId,
                                                               @RequestParam(defaultValue = "ALL") BookingState state) {
        List<BookingDto> list = service.getAllBookingsByBooker(bookerId, state);
        return ResponseEntity.ok().body(list);
    }

    @GetMapping("/owner")
    public ResponseEntity<List<BookingDto>> getBookingByOwner(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                                              @RequestParam(defaultValue = "ALL") BookingState state) {
        List<BookingDto> list = service.getAllBookingsByOwner(ownerId, state);
        return ResponseEntity.ok().body(list);
    }
}
