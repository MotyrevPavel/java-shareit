package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBooking;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {
    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    BookingServiceImpl bookingService;

    @Test
    void shouldCreateBookingSuccessfully_WhenValidData() {
        User booker = createUser(201L);
        Item item = createItem(101L);

        NewBooking newBooking = NewBooking.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        Booking savedBooking = createBooking(301L, item, booker, BookingState.WAITING);

        BookingDto expectedDto = BookingMapper.toBookingDto(savedBooking);

        Mockito.when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        Mockito.when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.save(Mockito.any(Booking.class))).thenReturn(savedBooking);

        BookingDto result = bookingService.create(newBooking, booker.getId());

        assertEquals(expectedDto, result);

        Mockito.verify(userRepository, Mockito.times(1)).findById(booker.getId());
        Mockito.verify(itemRepository, Mockito.times(1)).findById(item.getId());
        Mockito.verify(bookingRepository).save(Mockito.any(Booking.class));
    }

    @Test
    void shouldThrowNotFoundException_WhenUserNotFound() {
        NewBooking newBooking = NewBooking.builder()
                .itemId(101L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        Mockito.when(userRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bookingService.create(newBooking, 999L)
        );

        assertEquals("Пользователь не найден по ID " + 999L, exception.getMessage());

        Mockito.verify(userRepository, Mockito.times(1)).findById(999L);
        Mockito.verifyNoMoreInteractions(itemRepository, bookingRepository);
    }

    @Test
    void shouldThrowNotFoundException_WhenItemNotFound() {
        User booker = createUser(201L);
        NewBooking newBooking = NewBooking.builder()
                .itemId(999L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();


        Mockito.when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        Mockito.when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bookingService.create(newBooking, booker.getId())
        );

        assertEquals("Вещь не найдена по ID " + 999L, exception.getMessage());

        Mockito.verify(userRepository, Mockito.times(1)).findById(booker.getId());
        Mockito.verify(itemRepository, Mockito.times(1)).findById(999L);
        Mockito.verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void create_ShouldThrowValidationException_WhenItemNotAvailable() {
        User booker = createUser(201L);
        Item item = createItem(101L);
        item.setAvailable(false);

        NewBooking newBooking = NewBooking.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        Mockito.when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        Mockito.when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bookingService.create(newBooking, booker.getId())
        );

        assertEquals("Недоступна для бронирования вещь с ID " + item.getId(), exception.getMessage());

        Mockito.verify(userRepository, Mockito.times(1)).findById(booker.getId());
        Mockito.verify(itemRepository, Mockito.times(1)).findById(item.getId());
        Mockito.verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void shouldThrowNotFoundException_WhenBookingNotFound() {
        Mockito.when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bookingService.approve(999L, true, 999L)
        );

        assertEquals("Бронирования не найдено по ID " + 999L, exception.getMessage());

        Mockito.verify(bookingRepository, Mockito.times(1)).findById(999L);
        Mockito.verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void approve_ShouldThrowValidationException_WhenUserIsNotItemOwner() {
        User booker = createUser(201L);
        Item item = createItem(101L);

        Booking executedBooking = createBooking(301L, item, booker, BookingState.WAITING);

        Mockito.when(bookingRepository.findById(executedBooking.getId())).thenReturn(Optional.of(executedBooking));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bookingService.approve(executedBooking.getId(), true, booker.getId())
        );

        assertEquals("Подтвердить бронирование может только владелец вещи", exception.getMessage());

        Mockito.verify(bookingRepository, Mockito.times(1)).findById(executedBooking.getId());
        Mockito.verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void shouldReturnBookingDto_WhenUserIsOwner() {
        User booker = createUser(201L);
        Item item = createItem(101L);

        Booking executedBooking = createBooking(301L, item, booker, BookingState.WAITING);

        BookingDto expectedDto = BookingMapper.toBookingDto(executedBooking);

        Mockito.when(bookingRepository.findById(executedBooking.getId())).thenReturn(Optional.of(executedBooking));

        BookingDto result = bookingService.getById(executedBooking.getId(), item.getUser().getId());

        assertEquals(expectedDto, result);

        Mockito.verify(bookingRepository, Mockito.times(1)).findById(executedBooking.getId());
    }

    @Test
    void shouldReturnBookingDto_WhenUserIsBooker() {
        User booker = createUser(201L);
        Item item = createItem(101L);

        Booking executedBooking = createBooking(301L, item, booker, BookingState.WAITING);

        BookingDto expectedDto = BookingMapper.toBookingDto(executedBooking);

        Mockito.when(bookingRepository.findById(executedBooking.getId())).thenReturn(Optional.of(executedBooking));

        BookingDto result = bookingService.getById(executedBooking.getId(), booker.getId());

        assertEquals(expectedDto, result);

        Mockito.verify(bookingRepository, Mockito.times(1)).findById(executedBooking.getId());
    }

    @Test
    void shouldThrowNotFoundException_WhenUserHasNoAccess() {
        User user = createUser(203L);
        User booker = createUser(201L);
        Item item = createItem(101L);

        Booking executedBooking = createBooking(301L, item, booker, BookingState.WAITING);

        Mockito.when(bookingRepository.findById(executedBooking.getId())).thenReturn(Optional.of(executedBooking));

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bookingService.getById(executedBooking.getId(), user.getId())
        );

        assertEquals("У вас нет доступа для просмотра информации по этому бронированию", exception.getMessage());

        Mockito.verify(bookingRepository, Mockito.times(1)).findById(executedBooking.getId());
        Mockito.verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void shouldReturnEmptyList_WhenNoBookingsFound() {
        Mockito.when(bookingRepository.findByBookerIdAndStatus(999L, BookingState.APPROVED))
                .thenReturn(List.of());

        List<BookingDto> result = bookingService.getAllBookingsByBooker(999L, BookingState.APPROVED);

        assertTrue(result.isEmpty());

        Mockito.verify(bookingRepository, Mockito.times(1))
                .findByBookerIdAndStatus(999L, BookingState.APPROVED);
    }

    @Test
    void shouldReturnEmptyList_WhenNoBookingsByOwnerFound() {
        Mockito.when(userRepository.existsById(999L)).thenReturn(true);
        Mockito.when(bookingRepository.findByItemOwnerIdAndStatus(999L, BookingState.APPROVED))
                .thenReturn(List.of());

        List<BookingDto> result = bookingService.getAllBookingsByOwner(999L, BookingState.APPROVED);

        assertTrue(result.isEmpty());

        Mockito.verify(userRepository, Mockito.times(1)).existsById(999L);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findByItemOwnerIdAndStatus(999L, BookingState.APPROVED);
    }

    @Test
    void shouldThrowNotFoundException_WhenUserDoesNotExist() {
        Mockito.when(userRepository.existsById(999L)).thenReturn(false);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bookingService.getAllBookingsByOwner(999L, BookingState.APPROVED)
        );

        assertEquals("Не найден пользователь по ID " + 999L, exception.getMessage());

        Mockito.verify(userRepository, Mockito.times(1)).existsById(999L);
        Mockito.verifyNoMoreInteractions(bookingRepository);
    }

    private User createUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setName("UserName" + id);
        user.setEmail("email" + id + "@example.com");
        return user;
    }

    private Item createItem(Long id) {
        Item item = new Item();
        item.setId(id);
        item.setName("Item " + id);
        item.setDescription("ItemDescription " + id);
        item.setAvailable(true);
        item.setUser(createUser(202L));
        item.setRequest(null);
        return item;
    }

    private Booking createBooking(Long id, Item item, User booker, BookingState state) {
        Booking booking = new Booking();
        booking.setId(id);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(state);
        return booking;
    }
}