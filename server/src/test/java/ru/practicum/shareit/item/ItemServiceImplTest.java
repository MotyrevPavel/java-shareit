package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {
    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @InjectMocks
    ItemServiceImpl itemService;

    @Test
    void shouldCreateItemSuccessfully_WhenUserAndRequestExist() {
        NewItem newItem = NewItem.builder()
                .name("Тестовая вещь")
                .description("Описание тестовой вещи")
                .available(true)
                .requestId(301L)
                .build();

        User user = createUser(201L);
        ItemRequest itemRequest = createItemRequest(301L);

        Item item = ItemMapper.toItem(newItem, user, itemRequest);

        ItemDto expectedDto = ItemMapper.toDto(item);

        Mockito.when(userRepository.findById(201L)).thenReturn(Optional.of(user));
        Mockito.when(itemRequestRepository.findById(301L)).thenReturn(Optional.of(itemRequest));
        Mockito.when(itemRepository.save(Mockito.any(Item.class))).thenReturn(item);

        ItemDto result = itemService.create(newItem, 201L);

        assertEquals(expectedDto, result);

        Mockito.verify(userRepository, Mockito.times(1)).findById(201L);
        Mockito.verify(itemRequestRepository, Mockito.times(1)).findById(301L);
        Mockito.verify(itemRepository, Mockito.times(1)).save(item);
    }

    @Test
    void shouldCreateItemSuccessfully_WhenNoRequest() {
        NewItem newItem = NewItem.builder()
                .name("Тестовая вещь")
                .description("Описание тестовой вещи")
                .available(true)
                .build();

        User user = createUser(201L);

        Item item = ItemMapper.toItem(newItem, user, null);

        ItemDto expectedDto = ItemMapper.toDto(item);

        Mockito.when(userRepository.findById(201L)).thenReturn(Optional.of(user));
        Mockito.when(itemRepository.save(Mockito.any(Item.class))).thenReturn(item);

        ItemDto result = itemService.create(newItem, 201L);

        assertEquals(expectedDto, result);

        Mockito.verify(userRepository, Mockito.times(1)).findById(201L);
        Mockito.verify(itemRequestRepository, Mockito.never()).findById(Mockito.anyLong());
        Mockito.verify(itemRepository, Mockito.times(1)).save(item);
    }

    @Test
    void shouldThrowNotFoundException_WhenUserDoesNotExist() {
        NewItem newItem = NewItem.builder()
                .name("Тестовая вещь")
                .description("Описание тестовой вещи")
                .available(true)
                .build();

        Mockito.when(userRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemService.create(newItem, 999L)
        );

        assertTrue(exception.getMessage().contains("В базе отсутствует пользователь с ID " + 999L));

        Mockito.verify(itemRequestRepository, Mockito.never()).findById(Mockito.any());
        Mockito.verify(itemRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void shouldUpdateItemSuccessfully_WhenUserIsOwner() {
        Item existingItem = createItem(101L, 201L, 301L);

        UpdateItem updateItem = UpdateItem.builder()
                .name("New Name")
                .description("New Description")
                .available(false)
                .build();

        Item updatedItem = new Item();
        updatedItem.setId(existingItem.getId());
        updatedItem.setName(updateItem.getName());
        updatedItem.setDescription(updateItem.getDescription());
        updatedItem.setAvailable(updateItem.getAvailable());
        updatedItem.setUser(existingItem.getUser());
        updatedItem.setRequest(existingItem.getRequest());

        ItemDto expectedDto = ItemMapper.toDto(updatedItem);

        Mockito.when(itemRepository.findById(101L)).thenReturn(Optional.of(existingItem));
        Mockito.when(itemRepository.save(Mockito.any(Item.class))).thenReturn(updatedItem);

        ItemDto result = itemService.update(updateItem, 101L, 201L);

        assertEquals(expectedDto, result);

        Mockito.verify(itemRepository, Mockito.times(1)).findById(101L);
        Mockito.verify(itemRepository, Mockito.times(1)).save(updatedItem);
        Mockito.verifyNoMoreInteractions(itemRepository);
    }

    @Test
    void shouldThrowNotFoundException_WhenUserIsNotOwner() {
        Item item = createItem(101L, 201L, 301L);

        UpdateItem updateItem = UpdateItem.builder()
                .name("New Name")
                .description("New Description")
                .available(false)
                .build();

        Mockito.when(itemRepository.findById(101L)).thenReturn(Optional.of(item));

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemService.update(updateItem, 101L, 999L)
        );

        assertTrue(exception.getMessage().contains("Обновлять информацию может только владелец вещи"));

        Mockito.verify(itemRepository, Mockito.times(1)).findById(101L);
        Mockito.verify(itemRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void shouldThrowNotFoundException_WhenItemDoesNotExist() {
        UpdateItem updateItem = UpdateItem.builder()
                .name("New Name")
                .description("New Description")
                .available(false)
                .build();

        Mockito.when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemService.update(updateItem, 999L, 201L)
        );

        assertTrue(exception.getMessage().contains("В базе отсутствует вещь с ID " + 999L));

        Mockito.verify(itemRepository).findById(999L);
        Mockito.verify(itemRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void shouldThrowNotFoundException_WhenGetByIdItemDoesNotExist() {
        Mockito.when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemService.getById(999L)
        );

        assertTrue(exception.getMessage().contains("В базе отсутствует вещь с ID " + 999L));

        Mockito.verify(itemRepository).findById(999L);
        Mockito.verify(bookingRepository, Mockito.never()).findByItemId(Mockito.anyLong());
        Mockito.verify(commentRepository, Mockito.never()).findCommentsByItemId(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.never()).findByItemId(Mockito.anyLong());
    }

    @Test
    void shouldReturnItemFullDtoWithEmptyBookingsAndComments_WhenNoData() {
        Item item = createItem(101L, 201L, 301L);

        ItemFullDto expected = ItemFullDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .userId(item.getUser().getId())
                .nextBooking(null)
                .lastBooking(null)
                .comments(new ArrayList<>())
                .build();

        Mockito.when(itemRepository.findById(101L)).thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.findByItemId(101L)).thenReturn(new ArrayList<>());
        Mockito.when(commentRepository.findCommentsByItemId(101L)).thenReturn(new ArrayList<>());

        ItemFullDto result = itemService.getById(101L);

        assertEquals(expected, result);
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
        assertTrue(result.getComments().isEmpty());

        Mockito.verify(itemRepository).findById(101L);
        Mockito.verify(bookingRepository).findByItemId(101L);
        Mockito.verify(commentRepository).findCommentsByItemId(101L);
    }

    @Test
    void getAllByUserId_ShouldReturnEmptyList_WhenUserHasNoItems() {
        Mockito.when(itemRepository.findByUserId(999L)).thenReturn(Collections.emptyList());

        List<ItemFullDto> result = itemService.getAllByUserId(999L);

        assertTrue(result.isEmpty());
        Mockito.verify(itemRepository, Mockito.times(1)).findByUserId(999L);
        Mockito.verifyNoMoreInteractions(bookingRepository, commentRepository);
    }

    @Test
    void getAllByUserId_ShouldHandleItemsWithoutBookingsOrComments() {
        Item item = createItem(101L, 201L, 301L);
        List<Item> items = List.of(item);

        ItemFullDto itemDtoFirst = ItemFullDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .userId(item.getUser().getId())
                .nextBooking(null)
                .lastBooking(null)
                .comments(Collections.emptyList())
                .build();


        Mockito.when(itemRepository.findByUserId(201L)).thenReturn(items);
        Mockito.when(bookingRepository.findByItemOwnerIdAndStatus(201L, BookingState.APPROVED))
                .thenReturn(Collections.emptyList());
        Mockito.when(commentRepository.findCommentsByItemIds(Set.of(101L)))
                .thenReturn(Collections.emptyList());

        List<ItemFullDto> result = itemService.getAllByUserId(201L);

        assertEquals(1, result.size());
        assertEquals(itemDtoFirst, result.getFirst());
    }

    @Test
    void searchAvailableItemByParam_ShouldReturnEmptyList_WhenTextIsEmpty() {
        String text = "";

        List<ItemDto> result = itemService.searchAvailableItemByParam(text);

        assertTrue(result.isEmpty());
        Mockito.verifyNoInteractions(itemRepository);
    }

    @Test
    void searchAvailableItemByParam_ShouldReturnEmptyList_WhenTextIsBlank() {
        String text = "   ";

        List<ItemDto> result = itemService.searchAvailableItemByParam(text);

        assertTrue(result.isEmpty());
        Mockito.verifyNoInteractions(itemRepository);
    }

    @Test
    void searchAvailableItemByParam_ShouldReturnMappedDtoList_WhenItemsFound() {
        String text = "desc";

        Item itemFirst = createItem(101L, 201L, null);
        Item itemSecond = createItem(102L, 202L, null);
        List<Item> items = Arrays.asList(itemFirst, itemSecond);


        ItemDto expectedDtoFirst = ItemDto.builder()
                .id(itemFirst.getId())
                .name(itemFirst.getName())
                .description(itemFirst.getDescription())
                .available(itemFirst.getAvailable())
                .userId(itemFirst.getUser().getId())
                .build();
        ItemDto expectedDtoSecond = ItemDto.builder()
                .id(itemSecond.getId())
                .name(itemSecond.getName())
                .description(itemSecond.getDescription())
                .available(itemSecond.getAvailable())
                .userId(itemSecond.getUser().getId())
                .build();

        Mockito.when(itemRepository.searchAvailableItemByParam(text)).thenReturn(items);

        List<ItemDto> result = itemService.searchAvailableItemByParam(text);

        assertEquals(2, result.size());
        assertEquals(expectedDtoFirst, result.get(0));
        assertEquals(expectedDtoSecond, result.get(1));

        Mockito.verify(itemRepository, Mockito.times(1)).searchAvailableItemByParam(text);
    }

    @Test
    void searchAvailableItemByParam_ShouldReturnEmptyList_WhenNoItemsFound() {
        String text = "text";

        Mockito.when(itemRepository.searchAvailableItemByParam(text)).thenReturn(Collections.emptyList());

        List<ItemDto> result = itemService.searchAvailableItemByParam(text);

        assertTrue(result.isEmpty());
        Mockito.verify(itemRepository, Mockito.times(1)).searchAvailableItemByParam(text);
    }

    @Test
    void shouldCreateCommentSuccessfully_WhenBookingExistsAndCompleted() {
        NewComment newComment = NewComment.builder().text("commentText").build();

        User booker = createUser(201L);
        Item item = createItem(101L, 202L, null);
        Booking booking = createBooking(401L, LocalDateTime.now().minusHours(5),
                LocalDateTime.now().minusHours(2), item, booker);

        List<Booking> bookings = Collections.singletonList(booking);

        Comment savedComment = new Comment();
        savedComment.setId(601L);
        savedComment.setText("commentText");
        savedComment.setItem(item);
        savedComment.setAuthor(booker);
        savedComment.setCreated(LocalDateTime.now());

        CommentDto commentDtoExample = CommentDto.builder()
                .id(savedComment.getId())
                .text(savedComment.getText())
                .authorName(savedComment.getAuthor().getName())
                .created(savedComment.getCreated())
                .build();

        Mockito.when(bookingRepository.findByItemIdAndBookerIdAndStatus(101L, 201L,
                BookingState.APPROVED)).thenReturn(bookings);
        Mockito.when(commentRepository.save(Mockito.any(Comment.class))).thenReturn(savedComment);

        CommentDto result = itemService.createNewComment(101L, 201L, newComment);

        assertNotNull(result);
        assertEquals(commentDtoExample.getId(), result.getId());
        assertEquals(commentDtoExample.getText(), result.getText());
        assertEquals(commentDtoExample.getAuthorName(), result.getAuthorName());
        assertEquals(commentDtoExample.getCreated(), result.getCreated());

        Mockito.verify(bookingRepository, Mockito.times(1))
                .findByItemIdAndBookerIdAndStatus(101L, 201L, BookingState.APPROVED);
        Mockito.verify(commentRepository, Mockito.times(1)).save(Mockito.any(Comment.class));
    }

    @Test
    void shouldThrowValidationException_WhenNoBookingsFound() {
        NewComment newComment = NewComment.builder().text("commentText").build();

        Mockito.when(bookingRepository.findByItemIdAndBookerIdAndStatus(101L, 201L,
                BookingState.APPROVED)).thenReturn(Collections.emptyList());

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> itemService.createNewComment(101L, 201L, newComment)
        );

        assertEquals("У вас пока нет доступа к комментированию этой вещи", exception.getMessage());
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findByItemIdAndBookerIdAndStatus(101L, 201L, BookingState.APPROVED);
        Mockito.verifyNoMoreInteractions(commentRepository);
    }

    private User createUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setName("UserName" + id);
        user.setEmail("email" + id + "@example.com");
        return user;
    }

    private ItemRequest createItemRequest(Long id) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(id);
        return itemRequest;
    }

    private Item createItem(Long id, Long userId, Long itemRequestId) {
        User user = userId == null ? new User() : createUser(userId);
        ItemRequest itemRequest = itemRequestId == null ? new ItemRequest() : createItemRequest(itemRequestId);

        Item item = new Item();
        item.setId(id);
        item.setName("Item " + id);
        item.setDescription("ItemDescription " + id);
        item.setAvailable(true);
        item.setUser(user);
        item.setRequest(itemRequest);
        return item;
    }

    private Comment createComment(Long id, Item item, User author, LocalDateTime created) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setText("Comment text with id " + id);
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(created);
        return comment;
    }

    private Booking createBooking(Long id, LocalDateTime start, LocalDateTime end, Item item, User booker) {
        Booking booking = new Booking();
        booking.setId(id);
        booking.setStart(start);
        booking.setEnd(end);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingState.APPROVED);
        return booking;
    }
}