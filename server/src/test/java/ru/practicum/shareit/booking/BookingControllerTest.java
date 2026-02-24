package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBooking;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private BookingService service;

    @Test
    void shouldReturnCreatedBookingSuccessfully() throws Exception {
        Long bookerId = 1L;
        NewBooking newBooking = NewBooking.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusHours(1).withNano(0))
                .end(LocalDateTime.now().plusHours(2).withNano(0))
                .build();

        BookingDto expectedDto = BookingDto.builder()
                .id(1L)
                .start(newBooking.getStart())
                .end(newBooking.getEnd())
                .status("WAITING")
                .booker(UserDto.builder().id(bookerId).build())
                .item(ItemDto.builder().id(newBooking.getItemId()).build())
                .build();

        when(service.create(any(NewBooking.class), any(Long.class))).thenReturn(expectedDto);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newBooking)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedDto.getId()))
                .andExpect(jsonPath("$.start").value(expectedDto.getStart().toString()))
                .andExpect(jsonPath("$.end").value(expectedDto.getEnd().toString()))
                .andExpect(jsonPath("$.status").value(expectedDto.getStatus()))
                .andExpect(jsonPath("$.booker.id").value(expectedDto.getBooker().getId()))
                .andExpect(jsonPath("$.item.id").value(expectedDto.getItem().getId()));
    }

    @Test
    void shouldApproveBookingSuccessfully() throws Exception {
        Long bookingId = 1L;
        Long ownerId = 2L;
        boolean approved = true;

        BookingDto expectedDto = BookingDto.builder()
                .id(bookingId)
                .start(LocalDateTime.now().plusHours(1).withNano(0))
                .end(LocalDateTime.now().plusHours(2).withNano(0))
                .status("APPROVED")
                .booker(UserDto.builder().id(1L).build())
                .item(ItemDto.builder().id(1L).build())
                .build();

        when(service.approve(any(Long.class), any(Boolean.class), any(Long.class)))
                .thenReturn(expectedDto);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .param("approved", Boolean.toString(approved))
                        .header("X-Sharer-User-Id", ownerId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedDto.getId()))
                .andExpect(jsonPath("$.status").value(expectedDto.getStatus()))
                .andExpect(jsonPath("$.start").value(expectedDto.getStart().toString()))
                .andExpect(jsonPath("$.end").value(expectedDto.getEnd().toString()))
                .andExpect(jsonPath("$.booker.id").value(expectedDto.getBooker().getId()))
                .andExpect(jsonPath("$.item.id").value(expectedDto.getItem().getId()));
    }

    @Test
    void shouldGetBookingByIdSuccessfully() throws Exception {
        Long bookingId = 1L;
        Long userId = 1L;

        BookingDto expectedDto = BookingDto.builder()
                .id(bookingId)
                .start(LocalDateTime.now().plusHours(1).withNano(0))
                .end(LocalDateTime.now().plusHours(2).withNano(0))
                .status("WAITING")
                .booker(UserDto.builder().id(userId).build())
                .item(ItemDto.builder().id(1L).build())
                .build();

        when(service.getById(any(Long.class), any(Long.class))).thenReturn(expectedDto);

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedDto.getId()))
                .andExpect(jsonPath("$.status").value(expectedDto.getStatus()))
                .andExpect(jsonPath("$.start").value(expectedDto.getStart().toString()))
                .andExpect(jsonPath("$.end").value(expectedDto.getEnd().toString()))
                .andExpect(jsonPath("$.booker.id").value(expectedDto.getBooker().getId()))
                .andExpect(jsonPath("$.item.id").value(expectedDto.getItem().getId()));
    }

    @Test
    void shouldGetBookingsByBookerSuccessfully() throws Exception {
        Long bookerId = 1L;
        BookingState state = BookingState.ALL;

        List<BookingDto> expectedList = List.of(
                BookingDto.builder()
                        .id(1L)
                        .start(LocalDateTime.now().plusHours(1).withNano(0))
                        .end(LocalDateTime.now().plusHours(2).withNano(0))
                        .status("WAITING")
                        .booker(UserDto.builder().id(bookerId).build())
                        .item(ItemDto.builder().id(1L).build())
                        .build(),
                BookingDto.builder()
                        .id(2L)
                        .start(LocalDateTime.now().plusDays(1).withNano(0))
                        .end(LocalDateTime.now().plusDays(2).withNano(0))
                        .status("APPROVED")
                        .booker(UserDto.builder().id(bookerId).build())
                        .item(ItemDto.builder().id(2L).build())
                        .build()
        );

        when(service.getAllBookingsByBooker(any(Long.class), any(BookingState.class))).thenReturn(expectedList);

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", bookerId)
                        .param("state", state.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(expectedList.size()))
                .andExpect(jsonPath("$[0].id").value(expectedList.get(0).getId()))
                .andExpect(jsonPath("$[0].status").value(expectedList.get(0).getStatus()))
                .andExpect(jsonPath("$[1].id").value(expectedList.get(1).getId()))
                .andExpect(jsonPath("$[1].status").value(expectedList.get(1).getStatus()));
    }

    @Test
    void shouldGetBookingsByOwnerSuccessfully() throws Exception {
        Long ownerId = 1L;
        BookingState state = BookingState.ALL;

        List<BookingDto> expectedList = List.of(
                BookingDto.builder()
                        .id(1L)
                        .start(LocalDateTime.now().plusHours(1).withNano(0))
                        .end(LocalDateTime.now().plusHours(2).withNano(0))
                        .status("WAITING")
                        .booker(UserDto.builder().id(2L).build())
                        .item(ItemDto.builder().id(1L).build())
                        .build(),
                BookingDto.builder()
                        .id(2L)
                        .start(LocalDateTime.now().plusDays(1).withNano(0))
                        .end(LocalDateTime.now().plusDays(2).withNano(0))
                        .status("APPROVED")
                        .booker(UserDto.builder().id(3L).build())
                        .item(ItemDto.builder().id(2L).build())
                        .build()
        );

        when(service.getAllBookingsByOwner(any(Long.class), any(BookingState.class))).thenReturn(expectedList);

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", ownerId)
                        .param("state", state.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(expectedList.size()))
                .andExpect(jsonPath("$[0].id").value(expectedList.get(0).getId()))
                .andExpect(jsonPath("$[0].status").value(expectedList.get(0).getStatus()))
                .andExpect(jsonPath("$[1].id").value(expectedList.get(1).getId()))
                .andExpect(jsonPath("$[1].status").value(expectedList.get(1).getStatus()));
    }
}