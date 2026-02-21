package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@ExtendWith(SpringExtension.class)
class BookingServiceImplIntegrationTest {
    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    BookingServiceImpl bookingService;

    @Test
    void shouldCreateBookingSuccessfully_Integration() {
        User booker = createUser("booker", "booker@example.com");
        User bookerDb = userRepository.save(booker);

        User owner = createUser("owner", "owner@example.com");
        User ownerDb = userRepository.save(owner);

        Item item = createItem("Велосипед", "Горный велосипед", ownerDb, true);
        Item itemDb = itemRepository.save(item);

        NewBooking newBooking = NewBooking.builder()
                .itemId(itemDb.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(3))
                .build();

        BookingDto result = bookingService.create(newBooking, bookerDb.getId());

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(itemDb.getId(), result.getItem().getId());
        assertEquals(bookerDb.getId(), result.getBooker().getId());
        assertEquals(newBooking.getStart(), result.getStart());
        assertEquals(newBooking.getEnd(), result.getEnd());
        assertEquals("WAITING", result.getStatus());

        Booking savedBooking = bookingRepository.findById(result.getId()).orElse(null);
        assertNotNull(savedBooking);
        assertEquals(itemDb, savedBooking.getItem());
        assertEquals(bookerDb, savedBooking.getBooker());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUserDoesNotExist_Integration() {
        User owner = createUser("owner", "owner@example.com");
        User ownerDb = userRepository.save(owner);

        Item item = createItem("Велосипед", "Горный велосипед", ownerDb, true);
        Item itemDb = itemRepository.save(item);

        NewBooking newBooking = NewBooking.builder()
                .itemId(itemDb.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(3))
                .build();

        Long nonExistentUserId = 999L;

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bookingService.create(newBooking, nonExistentUserId)
        );

        assertEquals("Пользователь не найден по ID " + nonExistentUserId, exception.getMessage());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenItemDoesNotExist_Integration() {
        User booker = createUser("booker", "booker@example.com");
        User bookerDb = userRepository.save(booker);

        NewBooking newBooking = NewBooking.builder()
                .itemId(999L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(3))
                .build();

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bookingService.create(newBooking, bookerDb.getId())
        );

        assertEquals("Вещь не найдена по ID " + 999L, exception.getMessage());
    }

    @Test
    void shouldThrowValidationExceptionWhenItemIsNotAvailable_Integration() {
        User booker = createUser("booker", "booker@example.com");
        User bookerDb = userRepository.save(booker);

        User owner = createUser("owner", "owner@example.com");
        User ownerDb = userRepository.save(owner);

        Item item = createItem("Велосипед", "Горный велосипед", ownerDb, false);
        Item itemDb = itemRepository.save(item);

        NewBooking newBooking = NewBooking.builder()
                .itemId(itemDb.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(3))
                .build();

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bookingService.create(newBooking, bookerDb.getId())
        );

        assertEquals("Недоступна для бронирования вещь с ID " + item.getId(), exception.getMessage());
    }

    @Test
    void shouldApproveBookingSuccessfully_Integration() {
        User booker = createUser("booker", "booker@example.com");
        User bookerDb = userRepository.save(booker);

        User owner = createUser("owner", "owner@example.com");
        User ownerDb = userRepository.save(owner);

        Item item = createItem("Велосипед", "Горный велосипед", ownerDb, true);
        Item itemDb = itemRepository.save(item);

        NewBooking newBooking = NewBooking.builder()
                .itemId(itemDb.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(3))
                .build();

        BookingDto createdBooking = bookingService.create(newBooking, bookerDb.getId());

        BookingDto result = bookingService.approve(createdBooking.getId(), true, ownerDb.getId());

        assertNotNull(result);
        assertEquals(createdBooking.getId(), result.getId());
        assertEquals("APPROVED", result.getStatus());

        Booking updatedBooking = bookingRepository.findById(createdBooking.getId()).orElse(null);
        assertNotNull(updatedBooking);
        assertEquals(BookingState.APPROVED, updatedBooking.getStatus());
    }

    @Test
    void shouldRejectBookingSuccessfully_Integration() {
        User booker = createUser("booker", "booker@example.com");
        User bookerDb = userRepository.save(booker);

        User owner = createUser("owner", "owner@example.com");
        User ownerDb = userRepository.save(owner);

        Item item = createItem("Велосипед", "Горный велосипед", ownerDb, true);
        Item itemDb = itemRepository.save(item);

        NewBooking newBooking = NewBooking.builder()
                .itemId(itemDb.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(3))
                .build();

        BookingDto createdBooking = bookingService.create(newBooking, bookerDb.getId());

        BookingDto result = bookingService.approve(createdBooking.getId(), false, ownerDb.getId());

        assertNotNull(result);
        assertEquals(createdBooking.getId(), result.getId());
        assertEquals("REJECTED", result.getStatus());

        // Проверяем, что статус обновился в БД
        Booking updatedBooking = bookingRepository.findById(createdBooking.getId()).orElse(null);
        assertNotNull(updatedBooking);
        assertEquals(BookingState.REJECTED, updatedBooking.getStatus());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenBookingDoesNotExist_Integration() {
        User owner = createUser("owner", "owner@example.com");
        User ownerDb = userRepository.save(owner);

        Long nonExistentBookingId = 999L;

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bookingService.approve(nonExistentBookingId, true, ownerDb.getId())
        );

        assertEquals("Бронирования не найдено по ID " + nonExistentBookingId, exception.getMessage());
    }

    @Test
    void shouldThrowValidationExceptionWhenUserIsNotOwner_Integration() {
        User user = createUser("user", "user@example.com");
        User userDb = userRepository.save(user);

        User owner = createUser("owner", "owner@example.com");
        User ownerDb = userRepository.save(owner);
        Item item = createItem("Велосипед", "Горный велосипед", ownerDb, true);
        Item itemDb = itemRepository.save(item);

        User booker = createUser("booker", "booker@example.com");
        User bookerDb = userRepository.save(booker);

        NewBooking newBooking = NewBooking.builder()
                .itemId(itemDb.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(3))
                .build();

        BookingDto createdBooking = bookingService.create(newBooking, bookerDb.getId());

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bookingService.approve(createdBooking.getId(), true, userDb.getId())
        );

        assertEquals("Подтвердить бронирование может только владелец вещи", exception.getMessage());
    }

    @Test
    void shouldGetBookingByIdWhenUserIsOwner_Integration() {
        User booker = createUser("booker", "booker@example.com");
        User bookerDb = userRepository.save(booker);

        User owner = createUser("owner", "owner@example.com");
        User ownerDb = userRepository.save(owner);

        Item item = createItem("Велосипед", "Горный велосипед", ownerDb, true);
        Item itemDb = itemRepository.save(item);

        NewBooking newBooking = NewBooking.builder()
                .itemId(itemDb.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(3))
                .build();

        BookingDto createdBooking = bookingService.create(newBooking, bookerDb.getId());

        BookingDto result = bookingService.getById(createdBooking.getId(), ownerDb.getId());

        assertNotNull(result);
        assertEquals(createdBooking.getId(), result.getId());
        assertEquals(createdBooking.getItem(), result.getItem());
        assertEquals(createdBooking.getBooker(), result.getBooker());
    }

    @Test
    void shouldGetBookingByIdWhenUserIsBooker_Integration() {
        User booker = createUser("booker", "booker@example.com");
        User bookerDb = userRepository.save(booker);

        User owner = createUser("owner", "owner@example.com");
        User ownerDb = userRepository.save(owner);

        Item item = createItem("Велосипед", "Горный велосипед", ownerDb, true);
        Item itemDb = itemRepository.save(item);

        NewBooking newBooking = NewBooking.builder()
                .itemId(itemDb.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(3))
                .build();

        BookingDto createdBooking = bookingService.create(newBooking, bookerDb.getId());

        BookingDto result = bookingService.getById(createdBooking.getId(), bookerDb.getId());

        assertNotNull(result);
        assertEquals(createdBooking.getId(), result.getId());
        assertEquals(createdBooking.getItem(), result.getItem());
        assertEquals(createdBooking.getBooker(), result.getBooker());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenGetByIdBookingDoesNotExist_Integration() {
        User user = createUser("user", "user@example.com");
        User userDb = userRepository.save(user);

        Long nonExistentBookingId = 999L;

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bookingService.getById(nonExistentBookingId, userDb.getId())
        );

        assertEquals("Отсутствует бронирование с ID " + nonExistentBookingId, exception.getMessage());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUserHasNoAccess_Integration() {
        User user = createUser("user", "user@example.com");
        User userDb = userRepository.save(user);

        User booker = createUser("booker", "booker@example.com");
        User bookerDb = userRepository.save(booker);

        User owner = createUser("owner", "owner@example.com");
        User ownerDb = userRepository.save(owner);

        Item item = createItem("Велосипед", "Горный велосипед", ownerDb, true);
        Item itemDb = itemRepository.save(item);

        NewBooking newBooking = NewBooking.builder()
                .itemId(itemDb.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(3))
                .build();

        BookingDto createdBooking = bookingService.create(newBooking, bookerDb.getId());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bookingService.getById(createdBooking.getId(), userDb.getId())
        );

        assertEquals("У вас нет доступа для просмотра информации по этому бронированию", exception.getMessage());
    }

    @Test
    void shouldGetAllBookingsByBookerWithStatusWaiting_Integration() {
        User booker = createUser("booker", "booker@example.com");
        User bookerDb = userRepository.save(booker);

        User owner = createUser("owner", "owner@example.com");
        User ownerDb = userRepository.save(owner);

        Item item1 = createItem("Велосипед", "Горный велосипед", ownerDb, true);
        Item itemDb1 = itemRepository.save(item1);

        Item item2 = createItem("Стул", "Деревянный стул", ownerDb, true);
        Item itemDb2 = itemRepository.save(item2);

        NewBooking newBooking1 = NewBooking.builder()
                .itemId(itemDb1.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(3))
                .build();

        NewBooking newBooking2 = NewBooking.builder()
                .itemId(itemDb2.getId())
                .start(LocalDateTime.now().plusDays(4))
                .end(LocalDateTime.now().plusDays(6))
                .build();

        bookingService.create(newBooking1, bookerDb.getId());
        bookingService.create(newBooking2, bookerDb.getId());

        List<BookingDto> result = bookingService.getAllBookingsByBooker(bookerDb.getId(), BookingState.WAITING);

        assertNotNull(result);
        assertEquals(2, result.size());

        assertTrue(result.get(0).getStart().isAfter(result.get(1).getStart()),
                "Бронирования должны быть отсортированы по дате начала по убыванию");

        for (BookingDto bookingDto : result) {
            assertEquals("WAITING", bookingDto.getStatus());
            assertEquals(bookerDb.getId(), bookingDto.getBooker().getId());
        }
    }

    @Test
    void shouldGetAllBookingsByBookerWithStatusApproved_Integration() {
        User booker = createUser("booker", "booker@example.com");
        User bookerDb = userRepository.save(booker);

        User owner = createUser("owner", "owner@example.com");
        User ownerDb = userRepository.save(owner);

        Item item1 = createItem("Велосипед", "Горный велосипед", ownerDb, true);
        Item itemDb1 = itemRepository.save(item1);

        Item item2 = createItem("Стул", "Деревянный стул", ownerDb, true);
        Item itemDb2 = itemRepository.save(item2);

        NewBooking newBooking1 = NewBooking.builder()
                .itemId(itemDb1.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(3))
                .build();

        NewBooking newBooking2 = NewBooking.builder()
                .itemId(itemDb2.getId())
                .start(LocalDateTime.now().plusDays(4))
                .end(LocalDateTime.now().plusDays(6))
                .build();

        bookingService.create(newBooking2, bookerDb.getId());
        BookingDto createdBooking = bookingService.create(newBooking1, bookerDb.getId());
        bookingService.approve(createdBooking.getId(), true, ownerDb.getId());

        List<BookingDto> result = bookingService.getAllBookingsByBooker(bookerDb.getId(), BookingState.APPROVED);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("APPROVED", result.getFirst().getStatus());
        assertEquals(bookerDb.getId(), result.getFirst().getBooker().getId());
        assertEquals(createdBooking.getItem(), result.getFirst().getItem());
    }

    @Test
    void shouldReturnEmptyListWhenNoBookingsWithGivenState_Integration() {
        User booker = createUser("booker", "booker@example.com");
        User bookerDb = userRepository.save(booker);

        User owner = createUser("owner", "owner@example.com");
        User ownerDb = userRepository.save(owner);

        Item item1 = createItem("Велосипед", "Горный велосипед", ownerDb, true);
        Item itemDb1 = itemRepository.save(item1);

        Item item2 = createItem("Стул", "Деревянный стул", ownerDb, true);
        Item itemDb2 = itemRepository.save(item2);

        NewBooking newBooking1 = NewBooking.builder()
                .itemId(itemDb1.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(3))
                .build();

        NewBooking newBooking2 = NewBooking.builder()
                .itemId(itemDb2.getId())
                .start(LocalDateTime.now().plusDays(4))
                .end(LocalDateTime.now().plusDays(6))
                .build();

        bookingService.create(newBooking1, bookerDb.getId());
        bookingService.create(newBooking2, bookerDb.getId());

        List<BookingDto> result = bookingService.getAllBookingsByBooker(bookerDb.getId(), BookingState.REJECTED);

        assertNotNull(result);
        assertTrue(result.isEmpty(),
                "Должен возвращаться пустой список, когда нет бронирований с заданным статусом");
    }

    @Test
    void shouldGetAllBookingsByOwnerWithStatusWaiting_Integration() {
        User owner = createUser("owner", "owner@example.com");
        User ownerDb = userRepository.save(owner);

        Item item1 = createItem("Велосипед", "Горный велосипед", ownerDb, true);
        Item itemDb1 = itemRepository.save(item1);

        Item item2 = createItem("Стул", "Деревянный стул", ownerDb, true);
        Item itemDb2 = itemRepository.save(item2);

        User booker1 = createUser("booker1", "booker1@example.com");
        User bookerDb1 = userRepository.save(booker1);

        User booker2 = createUser("booker2", "booker2@example.com");
        User bookerDb2 = userRepository.save(booker2);

        NewBooking newBooking1 = NewBooking.builder()
                .itemId(itemDb1.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(3))
                .build();

        NewBooking newBooking2 = NewBooking.builder()
                .itemId(itemDb2.getId())
                .start(LocalDateTime.now().plusDays(4))
                .end(LocalDateTime.now().plusDays(6))
                .build();

        bookingService.create(newBooking1, bookerDb1.getId());
        bookingService.create(newBooking2, bookerDb2.getId());

        List<BookingDto> result = bookingService.getAllBookingsByOwner(ownerDb.getId(), BookingState.WAITING);

        assertNotNull(result);
        assertEquals(2, result.size());

        assertTrue(result.get(0).getStart().isAfter(result.get(1).getStart()),
                "Бронирования должны быть отсортированы по дате начала по убыванию");

        for (BookingDto bookingDto : result) {
            assertEquals("WAITING", bookingDto.getStatus());
            assertEquals(ownerDb.getId(), bookingDto.getItem().getUserId());
        }
    }

    @Test
    void shouldGetAllBookingsByOwnerWithStatusApproved_Integration() {
        User owner = createUser("owner", "owner@example.com");
        User ownerDb = userRepository.save(owner);

        Item item1 = createItem("Велосипед", "Горный велосипед", ownerDb, true);
        Item itemDb1 = itemRepository.save(item1);

        Item item2 = createItem("Стул", "Деревянный стул", ownerDb, true);
        Item itemDb2 = itemRepository.save(item2);

        User booker1 = createUser("booker1", "booker1@example.com");
        User bookerDb1 = userRepository.save(booker1);

        User booker2 = createUser("booker2", "booker2@example.com");
        User bookerDb2 = userRepository.save(booker2);

        NewBooking newBooking1 = NewBooking.builder()
                .itemId(itemDb1.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(3))
                .build();

        NewBooking newBooking2 = NewBooking.builder()
                .itemId(itemDb2.getId())
                .start(LocalDateTime.now().plusDays(4))
                .end(LocalDateTime.now().plusDays(6))
                .build();

        bookingService.create(newBooking1, bookerDb1.getId());
        BookingDto createdBooking = bookingService.create(newBooking2, bookerDb2.getId());
        bookingService.approve(createdBooking.getId(), true, ownerDb.getId());

        List<BookingDto> result = bookingService.getAllBookingsByOwner(ownerDb.getId(), BookingState.APPROVED);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("APPROVED", result.getFirst().getStatus());
        assertEquals(ownerDb.getId(), result.getFirst().getItem().getUserId());
    }

    @Test
    void shouldReturnEmptyListWhenNoBookingsByOwnerWithGivenState_Integration() {
        User user = createUser("user", "user@example.com");
        User userDb = userRepository.save(user);

        User owner = createUser("owner", "owner@example.com");
        User ownerDb = userRepository.save(owner);

        Item item1 = createItem("Велосипед", "Горный велосипед", ownerDb, true);
        Item itemDb1 = itemRepository.save(item1);

        Item item2 = createItem("Стул", "Деревянный стул", ownerDb, true);
        Item itemDb2 = itemRepository.save(item2);

        User booker1 = createUser("booker1", "booker1@example.com");
        User bookerDb1 = userRepository.save(booker1);

        User booker2 = createUser("booker2", "booker2@example.com");
        User bookerDb2 = userRepository.save(booker2);

        NewBooking newBooking1 = NewBooking.builder()
                .itemId(itemDb1.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(3))
                .build();

        NewBooking newBooking2 = NewBooking.builder()
                .itemId(itemDb2.getId())
                .start(LocalDateTime.now().plusDays(4))
                .end(LocalDateTime.now().plusDays(6))
                .build();

        bookingService.create(newBooking1, bookerDb1.getId());
        bookingService.create(newBooking2, bookerDb2.getId());

        List<BookingDto> result = bookingService.getAllBookingsByOwner(userDb.getId(), BookingState.WAITING);

        assertNotNull(result);
        assertTrue(result.isEmpty(),
                "Должен возвращаться пустой список, когда нет бронирований с заданным статусом");
    }

    @Test
    void shouldThrowNotFoundExceptionWhenOwnerDoesNotExist_Integration() {
        Long nonExistentOwnerId = 999L;

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bookingService.getAllBookingsByOwner(nonExistentOwnerId, BookingState.WAITING)
        );

        assertEquals("Не найден пользователь по ID " + nonExistentOwnerId, exception.getMessage());
    }

    private User createUser(String name, String email) {
        User user = new User();
        user.setId(null);
        user.setName(name);
        user.setEmail(email);
        return user;
    }

    private Item createItem(String name, String description, User user, Boolean available) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(available);
        item.setUser(user);
        item.setRequest(null);
        return item;
    }
}