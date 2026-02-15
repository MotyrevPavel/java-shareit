package ru.practicum.shareit.booking;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBooking;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public BookingDto create(NewBooking newBooking, Long bookerId) {
        log.info("Попытка создания бронирования {} от пользователя с ID {}", newBooking, bookerId);
        Optional<User> optionalUser = userRepository.findById(bookerId);
        if (optionalUser.isEmpty()) {
            log.error("Пользователь не найден по ID {}", bookerId);
            throw new NotFoundException("Пользователь не найден по ID " + bookerId);
        }
        Optional<Item> optionalItem = itemRepository.findById(newBooking.getItemId());
        if (optionalItem.isEmpty()) {
            log.error("Вещь не найдена по ID {}", newBooking.getItemId());
            throw new NotFoundException("Вещь не найдена по ID " + newBooking.getItemId());
        }
        Item item = optionalItem.get();
        if (!item.getAvailable()) {
            log.error("Вещь с ID {} недоступна для бронирования", newBooking.getItemId());
            throw new ValidationException("Недоступна для бронирования вещь с ID " + newBooking.getItemId());
        }
        Booking booking = BookingMapper.toBooking(newBooking, item, optionalUser.get());
        booking = bookingRepository.save(booking);
        BookingDto bookingDto = BookingMapper.toBookingDto(booking);
        log.info("Бронирование успешно создано {}", bookingDto);
        return bookingDto;
    }

    @Override
    public BookingDto approve(Long bookingId, Boolean approved, Long ownerId) {
        log.info("Попытка подтверждения бронирования с ID {} пользователем с ID {}", bookingId, ownerId);
        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
        if (bookingOptional.isEmpty()) {
            log.error("Бронирования не найдено по ID {}", bookingId);
            throw new NotFoundException("Бронирования не найдено по ID " + bookingId);
        }
        Booking booking = bookingOptional.get();
        Long itemOwnerId = booking.getItem().getUser().getId();
        if (!Objects.equals(itemOwnerId, ownerId)) {
            log.error("ID владельца вещи {}, ID пользователя подтверждаюшего бронь {}", itemOwnerId, ownerId);
            throw new ValidationException("Подтвердить бронирование может только владелец вещи");
        }
        if (approved) {
            log.info("Установлен статус бронирования {}", BookingState.APPROVED);
            booking.setStatus(BookingState.APPROVED);
        } else {
            log.info("Установлен новый статус бронирования {}", BookingState.REJECTED);
            booking.setStatus(BookingState.REJECTED);
        }
        bookingRepository.save(booking);
        BookingDto bookingDto = BookingMapper.toBookingDto(booking);
        log.info("Бронирование подтверждено {}", bookingDto);
        return bookingDto;
    }

    @Override
    public BookingDto getById(Long bookingId, Long userId) {
        log.info("Запрос информации по бронирования с ID {} от пользователя с ID {}", bookingId, userId);
        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
        if (bookingOptional.isEmpty()) {
            log.error("Отсутствует бронирование с ID {}", bookingId);
            throw new NotFoundException("Отсутствует бронирование с ID " + bookingId);
        }
        Booking booking = bookingOptional.get();
        Long itemOwnerId = booking.getItem().getUser().getId();
        Long bookerId = booking.getBooker().getId();
        if (!Objects.equals(itemOwnerId, userId) && !Objects.equals(bookerId, userId)) {
            log.error("ID владельца вещи {}, ID владельца брони {}, ID пользователя {}",
                    itemOwnerId, bookerId, userId);
            throw new ValidationException("У вас нет доступа для просмотра информации по этому бронированию");
        }
        BookingDto bookingDto = BookingMapper.toBookingDto(booking);
        log.info("Информация о бронировании получена {}", bookingDto);
        return bookingDto;
    }

    @Override
    public List<BookingDto> getAllBookingsByBooker(Long bookerId, BookingState state) {
        log.info("Запрос списка бронирований со статусом {} у пользователя с ID {}", state, bookerId);
        List<Booking> bookings = bookingRepository.findByBookerIdAndStatus(bookerId, state);
        List<BookingDto> bookingDtoList = bookings.stream()
                .map(BookingMapper::toBookingDto)
                .sorted(Comparator.comparing(BookingDto::getStart).reversed())
                .toList();
        log.info("Получен список бронирований {}", bookingDtoList);
        return bookingDtoList;
    }

    @Override
    public List<BookingDto> getAllBookingsByOwner(Long ownerId, BookingState state) {
        log.info("Запрос списка броней со статусом {} у владельца с ID {}", state, ownerId);
        if (!userRepository.existsById(ownerId)) {
            log.error("Не найден пользователь по ID {}", ownerId);
            throw new ValidationException("Не найден пользователь по ID " + ownerId);
        }
        List<Booking> bookings = bookingRepository.findByItemOwnerIdAndStatus(ownerId, state);
        List<BookingDto> bookingDtoList = bookings.stream()
                .map(BookingMapper::toBookingDto)
                .sorted(Comparator.comparing(BookingDto::getStart).reversed())
                .toList();
        log.info("Получен список броней {}", bookingDtoList);
        return bookingDtoList;
    }
}