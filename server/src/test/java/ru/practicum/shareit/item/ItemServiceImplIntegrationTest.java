package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@ExtendWith(SpringExtension.class)
class ItemServiceImplIntegrationTest {
    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    ItemServiceImpl itemService;

    @Test
    void shouldCreateItemSuccessfully_Integration() {
        User user = createUser("user", "user@example.com");
        User userDb = userRepository.save(user);

        NewItem newItem = createNewItem("Хороший молоток для ремонта", null);

        ItemDto result = itemService.create(newItem, userDb.getId());

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Молоток", result.getName());
        assertEquals("Хороший молоток для ремонта", result.getDescription());
        assertEquals(result.getUserId(), userDb.getId());
        assertTrue(result.getAvailable());

        Optional<Item> savedItem = itemRepository.findById(result.getId());
        assertTrue(savedItem.isPresent());

        Item dbItem = savedItem.get();
        assertEquals("Молоток", dbItem.getName());
        assertEquals("Хороший молоток для ремонта", dbItem.getDescription());
        assertEquals(dbItem.getUser().getId(), userDb.getId());
        assertTrue(dbItem.getAvailable());
    }

    @Test
    void shouldCreateItemWithRequestLink_Integration() {
        User user = createUser("user", "user@example.com");
        User userDb = userRepository.save(user);
        ItemRequest itemRequest = createItemRequest("itemRequestDescription", LocalDateTime.now(),
                userDb);
        ItemRequest itemRequestDb = itemRequestRepository.save(itemRequest);

        User ownerItem = createUser("owner", "owner@example.com");
        User ownerItemDb = userRepository.save(ownerItem);
        NewItem newItem = createNewItem("Хороший молоток", itemRequestDb.getId());

        ItemDto result = itemService.create(newItem, ownerItemDb.getId());

        assertNotNull(result);
        assertEquals(ownerItemDb.getId(), result.getUserId());

        Optional<Item> dbItem = itemRepository.findById(result.getId());
        assertTrue(dbItem.isPresent());
        assertNotNull(dbItem.get().getRequest());
        assertEquals(itemRequestDb.getId(), dbItem.get().getRequest().getId());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUserDoesNotExist_Integration() {
        Long nonExistentUserId = 999L;

        NewItem newItem = createNewItem("Хороший молоток для ремонта", null);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemService.create(newItem, nonExistentUserId)
        );

        assertTrue(exception.getMessage().contains("В базе отсутствует пользователь с ID " + nonExistentUserId),
                "Сообщение об ошибке должно содержать текст о не найденном пользователе");

        long totalItems = itemRepository.count();
        assertEquals(0, totalItems, "В БД не должно быть вещей после неудачного создания");
    }

    @Test
    void shouldCreateItemWhenRequestDoesNotExistButIdProvided_Integration() {
        User user = createUser("user", "user@example.com");
        User userDb = userRepository.save(user);

        NewItem newItem = createNewItem("Хороший молоток для ремонта", 999L);

        ItemDto result = itemService.create(newItem, userDb.getId());

        assertNotNull(result);

        Optional<Item> dbItem = itemRepository.findById(result.getId());
        assertTrue(dbItem.isPresent());
    }

    @Test
    void shouldUpdateItemSuccessfully_Integration() {
        User user = createUser("user", "user@example.com");
        User userDb = userRepository.save(user);

        NewItem newItem = createNewItem("Хороший молоток для ремонта", null);

        ItemDto oldItemDto = itemService.create(newItem, userDb.getId());

        UpdateItem updateItem = UpdateItem.builder()
                .name("Новый молоток")
                .description("Качественный новый молоток")
                .available(false)
                .build();

        ItemDto updatedItem = itemService.update(updateItem, oldItemDto.getId(), userDb.getId());

        assertNotNull(updatedItem);
        assertEquals(oldItemDto.getId(), updatedItem.getId());
        assertEquals("Новый молоток", updatedItem.getName());
        assertEquals("Качественный новый молоток", updatedItem.getDescription());
        assertFalse(updatedItem.getAvailable());

        Optional<Item> dbItem = itemRepository.findById(oldItemDto.getId());
        assertTrue(dbItem.isPresent());

        Item actualItem = dbItem.get();
        assertEquals("Новый молоток", actualItem.getName());
        assertEquals("Качественный новый молоток", actualItem.getDescription());
        assertFalse(actualItem.getAvailable());
    }

    @Test
    void shouldAllowPartialUpdate_Integration() {
        User user = createUser("user", "user@example.com");
        User userDb = userRepository.save(user);

        NewItem newItem = createNewItem("Хороший молоток для ремонта", null);

        ItemDto oldItemDto = itemService.create(newItem, userDb.getId());

        UpdateItem updateItem = UpdateItem.builder()
                .available(false)
                .build();

        ItemDto updatedItem = itemService.update(updateItem, oldItemDto.getId(), userDb.getId());

        assertEquals(oldItemDto.getName(), updatedItem.getName());
        assertEquals(oldItemDto.getDescription(), updatedItem.getDescription());
        assertFalse(updatedItem.getAvailable());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenItemDoesNotExist_Integration() {
        Long nonExistentItemId = 999L;
        Long userId = 1L;

        UpdateItem updateItem = UpdateItem.builder()
                .name("Обновлённое название")
                .build();

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemService.update(updateItem, nonExistentItemId, userId)
        );

        assertTrue(exception.getMessage().contains("В базе отсутствует вещь с ID " + nonExistentItemId),
                "Сообщение об ошибке должно указывать на отсутствие вещи");
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUserIsNotOwner_Integration() {
        User user = createUser("user", "user@example.com");
        User userDb = userRepository.save(user);

        NewItem newItem = createNewItem("Хороший молоток для ремонта", null);

        ItemDto oldItemDto = itemService.create(newItem, userDb.getId());

        UpdateItem updateItem = UpdateItem.builder()
                .available(false)
                .build();

        User userNotOwner = createUser("NotOwner", "notowner@example.com");

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemService.update(updateItem, oldItemDto.getId(), userNotOwner.getId())
        );

        assertTrue(exception.getMessage().contains("Обновлять информацию может только владелец вещи"),
                "Сообщение об ошибке должно указывать, что обновлять может только владелец");
    }

    @Test
    void shouldNotUpdateItemWhenUserIdIsNull_Integration() {
        User user = createUser("user", "user@example.com");
        User userDb = userRepository.save(user);

        NewItem newItem = createNewItem("Хороший молоток для ремонта", null);

        ItemDto oldItemDto = itemService.create(newItem, userDb.getId());

        UpdateItem updateItem = UpdateItem.builder()
                .available(false)
                .build();

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemService.update(updateItem, oldItemDto.getId(), null)
        );

        assertTrue(exception.getMessage().contains("Обновлять информацию может только владелец вещи"),
                "Сообщение об ошибке должно указывать, что обновлять может только владелец");
    }

    @Test
    void shouldGetItemFullDtoSuccessfully_Integration() {
        User user = createUser("user", "user@example.com");
        User userDb = userRepository.save(user);

        ItemRequest itemRequestDb = createItemRequestInDb();

        Item item = createItem("Молоток", "Good", userDb, itemRequestDb);
        Item itemDb = itemRepository.save(item);

        User booker1 = createUser("booker1", "booker1@example.com");
        User booker1Db = userRepository.save(booker1);
        BookingDto bookingDto1 = createBookingInDb(LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3), itemDb, booker1Db);

        User booker2 = createUser("booker2", "booker2@example.com");
        User booker2Db = userRepository.save(booker2);
        BookingDto bookingDto2 = createBookingInDb(LocalDateTime.now().plusHours(4),
                LocalDateTime.now().plusHours(5), itemDb, booker2Db);

        CommentDto commentDto = createCommentInDb("Comment1", itemDb, booker1Db,
                LocalDateTime.now().minusDays(2));
        CommentDto commentDto2 = createCommentInDb("Comment2", itemDb, booker2Db,
                LocalDateTime.now().minusDays(1));

        ItemFullDto result = itemService.getById(itemDb.getId());

        assertNotNull(result);
        assertEquals(itemDb.getId(), result.getId());
        assertEquals("Молоток", result.getName());
        assertEquals("Good", result.getDescription());
        assertTrue(result.getAvailable());
        assertEquals(userDb.getId(), result.getUserId());
        assertEquals(bookingDto1, result.getNextBooking());
        assertEquals(bookingDto2, result.getLastBooking());
        assertEquals(2, result.getComments().size());
        assertEquals(commentDto, result.getComments().getFirst());
        assertEquals(commentDto2, result.getComments().getLast());
    }

    @Test
    void shouldReturnItemWithEmptyBookingsAndComments_Integration() {
        User user = createUser("user", "user@example.com");
        User userDb = userRepository.save(user);

        ItemRequest itemRequestDb = createItemRequestInDb();

        Item item = createItem("Молоток", "Good", userDb, itemRequestDb);
        Item itemDb = itemRepository.save(item);

        ItemFullDto result = itemService.getById(itemDb.getId());

        assertNotNull(result);
        assertEquals(itemDb.getId(), result.getId());
        assertEquals("Молоток", result.getName());
        assertEquals("Good", result.getDescription());
        assertTrue(result.getAvailable());
        assertEquals(userDb.getId(), result.getUserId());
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
        assertTrue(result.getComments().isEmpty());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenGetByIdItemDoesNotExist_Integration() {
        Long nonExistentItemId = 999L;

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemService.getById(nonExistentItemId)
        );

        assertTrue(exception.getMessage().contains("В базе отсутствует вещь с ID " + nonExistentItemId),
                "Сообщение об ошибке должно указывать на отсутствие вещи");

        Optional<Item> nonExistentItem = itemRepository.findById(nonExistentItemId);
        assertFalse(nonExistentItem.isPresent(),
                "В БД не должно быть вещи с указанным ID");
    }

    @Test
    void shouldGetAllItemsForUserWithMultipleItems_Integration() {
        User user = createUser("user", "user@example.com");
        User userDb = userRepository.save(user);

        ItemRequest itemRequestDb1 = createItemRequestInDb();
        ItemRequest itemRequestDb2 = createItemRequestInDb();

        Item item = createItem("Молоток", "Good", userDb, itemRequestDb1);
        Item itemDb = itemRepository.save(item);

        Item item2 = createItem("Долото", "VeryGood", userDb, itemRequestDb2);
        Item itemDb2 = itemRepository.save(item2);

        User booker1 = createUser("booker1", "booker1@example.com");
        User booker1Db = userRepository.save(booker1);
        BookingDto bookingDto1 = createBookingInDb(LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3), itemDb, booker1Db);
        BookingDto bookingDto3 = createBookingInDb(LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3), itemDb2, booker1Db);

        User booker2 = createUser("booker2", "booker2@example.com");
        User booker2Db = userRepository.save(booker2);
        BookingDto bookingDto2 = createBookingInDb(LocalDateTime.now().plusHours(4),
                LocalDateTime.now().plusHours(5), itemDb, booker2Db);
        BookingDto bookingDto4 = createBookingInDb(LocalDateTime.now().plusHours(4),
                LocalDateTime.now().plusHours(5), itemDb2, booker2Db);

        CommentDto commentDto = createCommentInDb("Comment1", itemDb, booker1Db,
                LocalDateTime.now().minusDays(2));
        CommentDto commentDto2 = createCommentInDb("Comment2", itemDb2, booker2Db,
                LocalDateTime.now().minusDays(1));

        List<ItemFullDto> result = itemService.getAllByUserId(userDb.getId());

        ItemFullDto firstResult = result.getFirst();
        ItemFullDto secondResult = result.getLast();

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(itemDb.getId(), firstResult.getId());
        assertEquals("Молоток", firstResult.getName());
        assertEquals("Good", firstResult.getDescription());
        assertTrue(firstResult.getAvailable());
        assertEquals(userDb.getId(), firstResult.getUserId());
        assertEquals(bookingDto1, firstResult.getNextBooking());
        assertEquals(bookingDto2, firstResult.getLastBooking());
        assertEquals(1, firstResult.getComments().size());
        assertEquals(commentDto, firstResult.getComments().getFirst());

        assertEquals(itemDb2.getId(), secondResult.getId());
        assertEquals("Долото", secondResult.getName());
        assertEquals("VeryGood", secondResult.getDescription());
        assertTrue(secondResult.getAvailable());
        assertEquals(userDb.getId(), secondResult.getUserId());
        assertEquals(bookingDto3, secondResult.getNextBooking());
        assertEquals(bookingDto4, secondResult.getLastBooking());
        assertEquals(1, secondResult.getComments().size());
        assertEquals(commentDto2, secondResult.getComments().getFirst());
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoItems_Integration() {
        User user = createUser("user", "user@example.com");
        User userDb = userRepository.save(user);

        List<ItemFullDto> result = itemService.getAllByUserId(userDb.getId());

        assertNotNull(result);
        assertTrue(result.isEmpty(), "Список вещей должен быть пустым для пользователя без вещей");
    }

    @Test
    void shouldHandleItemsWithNoBookingsAndComments_Integration() {
        User user = createUser("user", "user@example.com");
        User userDb = userRepository.save(user);

        ItemRequest itemRequestDb1 = createItemRequestInDb();
        ItemRequest itemRequestDb2 = createItemRequestInDb();

        Item item = createItem("Молоток", "Good", userDb, itemRequestDb1);
        Item itemDb = itemRepository.save(item);

        Item item2 = createItem("Долото", "VeryGood", userDb, itemRequestDb2);
        Item itemDb2 = itemRepository.save(item2);

        List<ItemFullDto> result = itemService.getAllByUserId(userDb.getId());

        ItemFullDto firstResult = result.getFirst();
        ItemFullDto secondResult = result.getLast();

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(itemDb.getId(), firstResult.getId());
        assertEquals("Молоток", firstResult.getName());
        assertEquals("Good", firstResult.getDescription());
        assertTrue(firstResult.getAvailable());
        assertEquals(userDb.getId(), firstResult.getUserId());
        assertNull(firstResult.getNextBooking());
        assertNull(firstResult.getLastBooking());
        assertTrue(firstResult.getComments().isEmpty());

        assertEquals(itemDb2.getId(), secondResult.getId());
        assertEquals("Долото", secondResult.getName());
        assertEquals("VeryGood", secondResult.getDescription());
        assertTrue(secondResult.getAvailable());
        assertEquals(userDb.getId(), secondResult.getUserId());
        assertNull(secondResult.getNextBooking());
        assertNull(secondResult.getLastBooking());
        assertTrue(secondResult.getComments().isEmpty());
    }

    @Test
    void shouldReturnItemsWhenSearchTextMatches_Integration() {
        User user = createUser("user", "user@example.com");
        User userDb = userRepository.save(user);

        Item item1 = createItem("Строительный", "Хороший молоток для ремонта",
                userDb, null);
        itemRepository.save(item1);

        Item item2 = createItem("Отвёртка крестовая", "Набор отвёрток",
                userDb, null);
        itemRepository.save(item2);

        Item item3 = createItem("Молоток слесарный", "Профессиональный молоток",
                userDb, null);
        itemRepository.save(item3);

        String searchText = "Молот";

        List<ItemDto> result = itemService.searchAvailableItemByParam(searchText);

        assertNotNull(result);
        assertEquals(2, result.size(), "Должно быть найдено 2 доступных молотка");

        for (ItemDto itemDto : result) {
            assertTrue(itemDto.getName().toLowerCase().contains(searchText.toLowerCase())
                            || itemDto.getDescription().toLowerCase().contains(searchText.toLowerCase()),
                    "Название вещи должно содержать поисковый текст");
            assertTrue(itemDto.getAvailable(),
                    "В результат должны попадать только доступные вещи");
        }
    }

    @Test
    void shouldReturnEmptyListWhenNoMatchingItems_Integration() {
        User user = createUser("user", "user@example.com");
        User userDb = userRepository.save(user);

        Item item1 = createItem("Строительный", "Хороший молоток для ремонта",
                userDb, null);
        itemRepository.save(item1);

        Item item2 = createItem("Отвёртка крестовая", "Набор отвёрток",
                userDb, null);
        itemRepository.save(item2);

        String searchText = "стол";

        List<ItemDto> result = itemService.searchAvailableItemByParam(searchText);

        assertNotNull(result);
        assertTrue(result.isEmpty(),
                "Должен возвращаться пустой список, когда нет совпадений");
    }

    @Test
    void shouldReturnEmptyListWhenSearchTextIsEmpty_Integration() {
        User user = createUser("user", "user@example.com");
        User userDb = userRepository.save(user);

        Item item1 = createItem("Строительный", "Хороший молоток для ремонта",
                userDb, null);
        itemRepository.save(item1);

        Item item2 = createItem("Отвёртка крестовая", "Набор отвёрток",
                userDb, null);
        itemRepository.save(item2);

        String emptyText = "";

        List<ItemDto> result = itemService.searchAvailableItemByParam(emptyText);

        assertNotNull(result);
        assertTrue(result.isEmpty(),
                "Должен возвращаться пустой список для пустого поискового текста");
    }

    @Test
    void shouldReturnEmptyListWhenSearchTextIsBlank_Integration() {
        User user = createUser("user", "user@example.com");
        User userDb = userRepository.save(user);

        Item item1 = createItem("Строительный", "Хороший молоток для ремонта",
                userDb, null);
        itemRepository.save(item1);

        Item item2 = createItem("Отвёртка крестовая", "Набор отвёрток",
                userDb, null);
        itemRepository.save(item2);

        String blankText = "   ";

        List<ItemDto> result = itemService.searchAvailableItemByParam(blankText);

        assertNotNull(result);
        assertTrue(result.isEmpty(),
                "Должен возвращаться пустой список для пробельного поискового текста");
    }

    @Test
    void shouldCreateCommentSuccessfully_Integration() {
        User user = createUser("user", "user@example.com");
        User userDb = userRepository.save(user);

        Item item = createItem("Молоток", "Good", userDb, null);
        Item itemDb = itemRepository.save(item);

        User booker1 = createUser("booker1", "booker1@example.com");
        User booker1Db = userRepository.save(booker1);
        createBookingInDb(LocalDateTime.now().minusHours(2),
                LocalDateTime.now().minusHours(1), itemDb, booker1Db);

        NewComment newComment = NewComment.builder()
                .text("Отличный Молоток, очень доволен!")
                .build();

        CommentDto result = itemService.createNewComment(itemDb.getId(), booker1Db.getId(), newComment);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Отличный Молоток, очень доволен!", result.getText());
        assertEquals(booker1Db.getName(), result.getAuthorName());
        assertNotNull(result.getCreated());

        Optional<Comment> comment = commentRepository.findById(result.getId());
        assertTrue(comment.isPresent());
        assertEquals("Отличный Молоток, очень доволен!", comment.get().getText());
    }

    @Test
    void shouldThrowValidationExceptionWhenNoCompletedBookings_Integration() {
        User user = createUser("user", "user@example.com");
        User userDb = userRepository.save(user);

        Item item = createItem("Молоток", "Good", userDb, null);
        Item itemDb = itemRepository.save(item);

        User booker1 = createUser("booker1", "booker1@example.com");
        User booker1Db = userRepository.save(booker1);
        createBookingInDb(LocalDateTime.now().minusHours(2),
                LocalDateTime.now().plusHours(1), itemDb, booker1Db);

        NewComment newComment = NewComment.builder()
                .text("Отличный Молоток, очень доволен!")
                .build();

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> itemService.createNewComment(itemDb.getId(), booker1Db.getId(), newComment)
        );

        assertEquals("У вас пока нет доступа к комментированию этой вещи",
                exception.getMessage());
    }

    @Test
    void shouldThrowValidationExceptionWhenNoBookings_Integration() {
        User user = createUser("user", "user@example.com");
        User userDb = userRepository.save(user);

        Item item = createItem("Молоток", "Good", userDb, null);
        Item itemDb = itemRepository.save(item);

        User booker1 = createUser("booker1", "booker1@example.com");
        User booker1Db = userRepository.save(booker1);
        createBookingInDb(LocalDateTime.now().minusHours(2),
                LocalDateTime.now().minusHours(1), itemDb, booker1Db);

        User booker2 = createUser("booker2", "booker2@example.com");
        User booker2Db = userRepository.save(booker2);

        NewComment newComment = NewComment.builder()
                .text("Отличный Молоток, очень доволен!")
                .build();

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> itemService.createNewComment(itemDb.getId(), booker2Db.getId(), newComment)
        );

        assertEquals("У вас пока нет доступа к комментированию этой вещи",
                exception.getMessage());
    }


    private User createUser(String name, String email) {
        User user = new User();
        user.setId(null);
        user.setName(name);
        user.setEmail(email);
        return user;
    }

    private NewItem createNewItem(String description, Long requestId) {
        return NewItem.builder()
                .name("Молоток")
                .description(description)
                .available(true)
                .requestId(requestId)
                .build();
    }

    private ItemRequest createItemRequest(String description, LocalDateTime created, User user) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription(description);
        itemRequest.setCreated(created);
        itemRequest.setUser(user);
        itemRequest.setItems(null);
        return itemRequest;
    }

    private Item createItem(String name, String description, User user, ItemRequest itemRequest) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(true);
        item.setUser(user);
        item.setRequest(itemRequest);
        return item;
    }

    private Booking createBooking(LocalDateTime start, LocalDateTime end, Item item, User booker) {
        Booking booking = new Booking();
        booking.setStart(start);
        booking.setEnd(end);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingState.APPROVED);
        return booking;
    }

    private Comment createComment(String text, Item item, User author, LocalDateTime created) {
        Comment comment = new Comment();
        comment.setText(text);
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(created);
        return comment;
    }

    private CommentDto createCommentInDb(String text, Item itemDb, User bookerDb, LocalDateTime dateTime) {
        Comment comment = createComment(text, itemDb, bookerDb, dateTime);
        Comment commentDb = commentRepository.save(comment);
        return CommentMapper.toDto(commentDb);
    }

    private BookingDto createBookingInDb(LocalDateTime start, LocalDateTime end, Item itemDb, User userDb) {
        Booking booking2 = createBooking(start,
                end, itemDb, userDb);
        Booking booking2Db = bookingRepository.save(booking2);
        return BookingMapper.toBookingDto(booking2Db);
    }

    private ItemRequest createItemRequestInDb() {
        User userRequest = createUser("userRequest", "userRequest@example.com");
        User userRequestDb = userRepository.save(userRequest);
        ItemRequest itemRequest = createItemRequest("Нужен молоток", LocalDateTime.now().minusDays(2),
                userRequestDb);
        return itemRequestRepository.save(itemRequest);
    }
}