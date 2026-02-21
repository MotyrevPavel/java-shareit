package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.comment.CommentMapper;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.dto.NewComment;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemFullDto;
import ru.practicum.shareit.item.dto.NewItem;
import ru.practicum.shareit.item.dto.UpdateItem;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    public ItemDto create(NewItem newItem, Long userId) {
        log.info("Попытка добавления новой вещи {}", newItem);
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            log.error("В базе отсутствует пользователь с ID {}", userId);
            throw new NotFoundException("В базе отсутствует пользователь с ID " + userId);
        }
        ItemRequest itemRequest;
        if (newItem.getRequestId() == null) {
            itemRequest = null;
        } else {
            Optional<ItemRequest> optionalItemRequest = itemRequestRepository.findById(newItem.getRequestId());
            itemRequest = optionalItemRequest.orElse(null);
        }
        User user = optionalUser.get();
        Item item = ItemMapper.toItem(newItem, user, itemRequest);
        item = itemRepository.save(item);
        log.info("Вещь добавлена в репозиторий {}", item);
        return ItemMapper.toDto(item);
    }

    @Override
    public ItemDto update(UpdateItem updateItem, Long itemId, Long userId) {
        log.info("Попытка обновления вещи {}", updateItem);
        Item item = getItemFromDB(itemId);
        log.info("Получили по ID {} из репозитория вещь {}", itemId, item);
        if (!Objects.equals(item.getUser().getId(), userId)) {
            log.error("Попытка обновления пользователя с ID {} информации о вещи владельца с ID {}",
                    item.getUser().getId(), userId);
            throw new NotFoundException("Обновлять информацию может только владелец вещи");
        }
        ItemMapper.toItem(item, updateItem);
        itemRepository.save(item);
        log.info("Обновили данные о вещи в БД {}", item);
        return ItemMapper.toDto(item);
    }

    @Override
    @Transactional
    public ItemFullDto getById(Long id) {
        log.info("Попытка получения вещи по {}", id);
        Item item = getItemFromDB(id);
        List<Comment> comments = commentRepository.findCommentsByItemId(id);
        List<Booking> bookings = bookingRepository.findByItemId(id);
        List<ItemFullDto> listFullItemDto = collectDto(List.of(item), bookings, comments);
        log.info("Вещь по ID {} получена {}", id, item);
        return listFullItemDto.getFirst();
    }

    @Override
    @Transactional
    public List<ItemFullDto> getAllByUserId(Long userId) {
        log.info("Попытка получения всех вещей по пользователю с ID {}", userId);
        List<Item> items = itemRepository.findByUserId(userId);

        if (items.isEmpty()) {
            log.info("У пользователя {} нет вещей", userId);
            return Collections.emptyList();
        }

        Set<Long> itemIds = items.stream().map(Item::getId).collect(Collectors.toSet());

        List<Booking> bookings = bookingRepository.findByItemOwnerIdAndStatus(userId, BookingState.APPROVED);
        List<Comment> comments = commentRepository.findCommentsByItemIds(itemIds);

        List<ItemFullDto> itemDtoList = collectDto(items, bookings, comments);

        log.info("Получено {} вещей для пользователя с ID {}", itemDtoList.size(), userId);
        return itemDtoList;
    }

    @Override
    public List<ItemDto> searchAvailableItemByParam(String text) {
        log.info("Попытка получения всех вещей по параметру {}", text);
        if (text.trim().isEmpty()) {
            return new ArrayList<>();
        }
        List<Item> items = itemRepository.searchAvailableItemByParam(text);
        log.info("Получен по параметру{} список вещей {}", text, items);
        return items.stream()
                .map(ItemMapper::toDto)
                .toList();
    }

    @Override
    public CommentDto createNewComment(Long itemId, Long userId, NewComment newComment) {
        log.info("Создание комментария пользователем с ID {} у вещи с ID {}", userId, itemId);
        List<Booking> bookings =
                bookingRepository.findByItemIdAndBookerIdAndStatus(itemId, userId, BookingState.APPROVED);
        bookings = bookings.stream()
                .filter(booking -> booking.getEnd().isBefore(LocalDateTime.now()))
                .toList();
        if (bookings.isEmpty()) {
            log.error("Ошибка комментирования пользователем с ID {} вещи с ID {}", userId, itemId);
            throw new ValidationException("У вас пока нет доступа к комментированию этой вещи");
        }
        Booking booking = bookings.getFirst();
        Comment comment = CommentMapper.toComment(newComment, booking.getItem(), booking.getBooker());
        comment = commentRepository.save(comment);
        CommentDto commentDto = CommentMapper.toDto(comment);
        log.info("Комментарий создан {}", commentDto);
        return commentDto;
    }

    private Item getItemFromDB(Long id) {
        Optional<Item> optionalItem = itemRepository.findById(id);
        if (optionalItem.isEmpty()) {
            log.error("В базе отсутствует вещь с ID {}", id);
            throw new NotFoundException("В базе отсутствует вещь с ID " + id);
        }
        return optionalItem.get();
    }

    private List<ItemFullDto> collectDto(List<Item> items, List<Booking> bookings, List<Comment> comments) {
        Map<Long, List<Booking>> bookingsByItemId = bookings.stream()
                .collect(Collectors.groupingBy(b -> b.getItem().getId()));
        Map<Long, List<Comment>> commentsByItemId = comments.stream()
                .collect(Collectors.groupingBy(c -> c.getItem().getId()));

        return items.stream()
                .map(item -> {
                    List<Comment> itemComments = commentsByItemId.getOrDefault(item.getId(), Collections.emptyList());
                    List<CommentDto> commentDtoList = itemComments.stream()
                            .map(CommentMapper::toDto)
                            .toList();

                    List<Booking> itemBookings = bookingsByItemId.getOrDefault(item.getId(), Collections.emptyList());
                    List<Booking> sortedBookings = itemBookings.stream()
                            .filter(booking -> booking.getStart().isAfter(LocalDateTime.now(ZoneId.of("Europe/Moscow"))))
                            .sorted(Comparator.comparing(Booking::getStart))
                            .toList();

                    Booking nextBooking = !sortedBookings.isEmpty() ? sortedBookings.getFirst() : null;
                    Booking lastBooking = !sortedBookings.isEmpty() ? sortedBookings.getLast() : null;
                    BookingDto nextBookingDto = null;
                    BookingDto lastBookingDto = null;

                    if (nextBooking != null) {
                        nextBookingDto = BookingMapper.toBookingDto(nextBooking);
                    }

                    if (lastBooking != null) {
                        lastBookingDto = BookingMapper.toBookingDto(lastBooking);
                    }

                    return ItemMapper.toItemFullDto(item, nextBookingDto, lastBookingDto, commentDtoList);
                })
                .toList();
    }
}