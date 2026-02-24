package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingClient bookingClient;

    @Test
    void shouldReturnCreatedWhenBookItemWithValidData() throws Exception {
        long userId = 1L;
        BookItemRequestDto requestDto = new BookItemRequestDto();
        requestDto.setItemId(1L);
        requestDto.setStart(LocalDateTime.now().plusHours(1));
        requestDto.setEnd(LocalDateTime.now().plusHours(2));

        ResponseEntity<Object> expectedResponse = ResponseEntity.status(201).body("Booking created");
        when(bookingClient.bookItem(eq(userId), any(BookItemRequestDto.class)))
                .thenReturn(expectedResponse);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Booking created"));

        verify(bookingClient, times(1)).bookItem(eq(userId), any(BookItemRequestDto.class));
    }

    @Test
    void shouldReturnBadRequestWhenBookItemWithPastStart() throws Exception {
        long userId = 1L;
        BookItemRequestDto requestDto = new BookItemRequestDto();
        requestDto.setItemId(1L);
        requestDto.setStart(LocalDateTime.now().minusHours(1)); // Прошлое время
        requestDto.setEnd(LocalDateTime.now().plusHours(1));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(
                        MethodArgumentNotValidException.class,
                        result.getResolvedException()));
    }

    @Test
    void shouldReturnBadRequestWhenBookItemWithoutUserIdHeader() throws Exception {
        BookItemRequestDto requestDto = new BookItemRequestDto();
        requestDto.setItemId(1L);
        requestDto.setStart(LocalDateTime.now().plusHours(1));
        requestDto.setEnd(LocalDateTime.now().plusHours(2));

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenBookItemWithNullStart() throws Exception {
        long userId = 1L;
        BookItemRequestDto requestDto = new BookItemRequestDto();
        requestDto.setItemId(1L);
        requestDto.setStart(null); // Null start
        requestDto.setEnd(LocalDateTime.now().plusHours(2));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(
                        MethodArgumentNotValidException.class,
                        result.getResolvedException()));
    }

    @Test
    void shouldReturnBadRequestWhenBookItemWithNullEnd() throws Exception {
        long userId = 1L;
        BookItemRequestDto requestDto = new BookItemRequestDto();
        requestDto.setItemId(1L);
        requestDto.setStart(LocalDateTime.now().plusHours(1));
        requestDto.setEnd(null); // Null end

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(
                        MethodArgumentNotValidException.class,
                        result.getResolvedException()));
    }

    @Test
    void shouldReturnOkWhenApproveBookingWithValidData() throws Exception {
        Long bookingId = 1L;
        Long userId = 1L;
        Boolean approved = true;

        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().body("Booking approved");
        when(bookingClient.patchBooking(eq(bookingId), eq(approved), eq(userId)))
                .thenReturn(expectedResponse);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .param("approved", approved.toString())
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().string("Booking approved"));

        verify(bookingClient, times(1)).patchBooking(eq(bookingId), eq(approved), eq(userId));
    }

    @Test
    void shouldReturnOkWhenRejectBookingWithValidData() throws Exception {
        Long bookingId = 1L;
        Long userId = 1L;
        Boolean approved = false;

        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().body("Booking rejected");
        when(bookingClient.patchBooking(eq(bookingId), eq(approved), eq(userId)))
                .thenReturn(expectedResponse);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .param("approved", approved.toString())
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().string("Booking rejected"));

        verify(bookingClient, times(1)).patchBooking(eq(bookingId), eq(approved), eq(userId));
    }

    @Test
    void shouldReturnBadRequestWhenApproveBookingWithoutUserIdHeader() throws Exception {
        Long bookingId = 1L;
        Boolean approved = true;

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .param("approved", approved.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenApproveBookingWithNonNumericBookingId() throws Exception {
        String invalidBookingId = "abc";
        Long userId = 1L;
        Boolean approved = true;

        mockMvc.perform(patch("/bookings/{bookingId}", invalidBookingId)
                        .param("approved", approved.toString())
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(
                        MethodArgumentTypeMismatchException.class,
                        result.getResolvedException()));
    }

    @Test
    void shouldReturnBadRequestWhenApproveBookingWithoutApprovedParam() throws Exception {
        Long bookingId = 1L;
        Long userId = 1L;

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenApproveBookingWithInvalidApprovedParam() throws Exception {
        Long bookingId = 1L;
        Long userId = 1L;
        String invalidApprovedValue = "maybe"; // Некорректное значение параметра approved

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .param("approved", invalidApprovedValue)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(
                        MethodArgumentTypeMismatchException.class,
                        result.getResolvedException()));
    }

    @Test
    void shouldReturnOkWhenGetBookingsWithDefaultState() throws Exception {
        long userId = 1L;
        BookingState expectedState = BookingState.ALL;

        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().body("Bookings list");
        when(bookingClient.getBookings(eq(userId), eq(expectedState)))
                .thenReturn(expectedResponse);

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().string("Bookings list"));

        verify(bookingClient, times(1)).getBookings(eq(userId), eq(expectedState));
    }

    @Test
    void shouldReturnOkWhenGetBookingsWithCurrentState() throws Exception {
        long userId = 1L;
        String stateParam = "CURRENT";
        BookingState expectedState = BookingState.CURRENT;

        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().body("Current bookings");
        when(bookingClient.getBookings(eq(userId), eq(expectedState)))
                .thenReturn(expectedResponse);

        mockMvc.perform(get("/bookings")
                        .param("state", stateParam)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().string("Current bookings"));

        verify(bookingClient, times(1)).getBookings(eq(userId), eq(expectedState));
    }

    @Test
    void shouldReturnOkWhenGetBookingsWithFutureState() throws Exception {
        long userId = 1L;
        String stateParam = "FUTURE";
        BookingState expectedState = BookingState.FUTURE;

        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().body("Future bookings");
        when(bookingClient.getBookings(eq(userId), eq(expectedState)))
                .thenReturn(expectedResponse);

        mockMvc.perform(get("/bookings")
                        .param("state", stateParam)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().string("Future bookings"));

        verify(bookingClient, times(1)).getBookings(eq(userId), eq(expectedState));
    }

    @Test
    void shouldReturnOkWhenGetBookingsWithPastState() throws Exception {
        long userId = 1L;
        String stateParam = "PAST";
        BookingState expectedState = BookingState.PAST;

        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().body("Past bookings");
        when(bookingClient.getBookings(eq(userId), eq(expectedState)))
                .thenReturn(expectedResponse);

        mockMvc.perform(get("/bookings")
                        .param("state", stateParam)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().string("Past bookings"));

        verify(bookingClient, times(1)).getBookings(eq(userId), eq(expectedState));
    }

    @Test
    void shouldReturnBadRequestWhenGetBookingsWithoutUserIdHeader() throws Exception {
        mockMvc.perform(get("/bookings"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnOkWhenGetBookingWithValidData() throws Exception {
        long userId = 1L;
        Long bookingId = 1L;

        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().body("Booking details");
        when(bookingClient.getBooking(eq(userId), eq(bookingId)))
                .thenReturn(expectedResponse);

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().string("Booking details"));

        verify(bookingClient, times(1)).getBooking(eq(userId), eq(bookingId));
    }

    @Test
    void shouldReturnBadRequestWhenGetBookingWithoutUserIdHeader() throws Exception {
        Long bookingId = 1L;

        mockMvc.perform(get("/bookings/{bookingId}", bookingId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenGetBookingWithNonNumericBookingId() throws Exception {
        String invalidBookingId = "abc";
        Long userId = 1L;

        mockMvc.perform(get("/bookings/{bookingId}", invalidBookingId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(
                        MethodArgumentTypeMismatchException.class,
                        result.getResolvedException()));
    }

    @Test
    void shouldReturnOkWhenGetBookingByOwnerWithDefaultState() throws Exception {
        Long ownerId = 1L;
        BookingState expectedState = BookingState.ALL;

        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().body("Owner bookings list");
        when(bookingClient.getAllBookingsByOwner(eq(ownerId), eq(expectedState)))
                .thenReturn(expectedResponse);

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", ownerId))
                .andExpect(status().isOk())
                .andExpect(content().string("Owner bookings list"));

        verify(bookingClient, times(1)).getAllBookingsByOwner(eq(ownerId), eq(expectedState));
    }

    @Test
    void shouldReturnOkWhenGetBookingByOwnerWithCurrentState() throws Exception {
        Long ownerId = 1L;
        String stateParam = "CURRENT";
        BookingState expectedState = BookingState.CURRENT;

        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().body("Current owner bookings");
        when(bookingClient.getAllBookingsByOwner(eq(ownerId), eq(expectedState)))
                .thenReturn(expectedResponse);

        mockMvc.perform(get("/bookings/owner")
                        .param("state", stateParam)
                        .header("X-Sharer-User-Id", ownerId))
                .andExpect(status().isOk())
                .andExpect(content().string("Current owner bookings"));

        verify(bookingClient, times(1)).getAllBookingsByOwner(eq(ownerId), eq(expectedState));
    }

    @Test
    void shouldReturnOkWhenGetBookingByOwnerWithFutureState() throws Exception {
        Long ownerId = 1L;
        String stateParam = "FUTURE";
        BookingState expectedState = BookingState.FUTURE;

        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().body("Future owner bookings");
        when(bookingClient.getAllBookingsByOwner(eq(ownerId), eq(expectedState)))
                .thenReturn(expectedResponse);

        mockMvc.perform(get("/bookings/owner")
                        .param("state", stateParam)
                        .header("X-Sharer-User-Id", ownerId))
                .andExpect(status().isOk())
                .andExpect(content().string("Future owner bookings"));

        verify(bookingClient, times(1)).getAllBookingsByOwner(eq(ownerId), eq(expectedState));
    }

    @Test
    void shouldReturnOkWhenGetBookingByOwnerWithPastState() throws Exception {
        Long ownerId = 1L;
        String stateParam = "PAST";
        BookingState expectedState = BookingState.PAST;

        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().body("Past owner bookings");
        when(bookingClient.getAllBookingsByOwner(eq(ownerId), eq(expectedState)))
                .thenReturn(expectedResponse);

        mockMvc.perform(get("/bookings/owner")
                        .param("state", stateParam)
                        .header("X-Sharer-User-Id", ownerId))
                .andExpect(status().isOk())
                .andExpect(content().string("Past owner bookings"));

        verify(bookingClient, times(1)).getAllBookingsByOwner(eq(ownerId), eq(expectedState));
    }

    @Test
    void shouldReturnBadRequestWhenGetBookingByOwnerWithoutUserIdHeader() throws Exception {
        mockMvc.perform(get("/bookings/owner"))
                .andExpect(status().isBadRequest());
    }
}