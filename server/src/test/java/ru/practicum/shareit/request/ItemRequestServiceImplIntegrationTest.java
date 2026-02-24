package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithAnswersDto;
import ru.practicum.shareit.request.dto.NewItemRequest;
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
public class ItemRequestServiceImplIntegrationTest {
    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestService itemRequestService;

    @Test
    void shouldCreateItemRequestSuccessfully_Integration() {
        User user = createUser("user", "user@user.com");
        User userDb = userRepository.save(user);

        NewItemRequest newItemRequest = createNewItemRequest("Молоток");

        ItemRequestDto result = itemRequestService.create(userDb.getId(), newItemRequest);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Молоток", result.getDescription());
        assertNotNull(result.getCreated());

        Optional<ItemRequest> savedRequest = itemRequestRepository.findById(result.getId());
        assertTrue(savedRequest.isPresent());

        ItemRequest dbRequest = savedRequest.get();
        assertEquals("Молоток", dbRequest.getDescription());
        assertNotNull(dbRequest.getCreated());
        assertEquals(userDb.getId(), dbRequest.getUser().getId());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUserDoesNotExist_Integration() {
        Long nonExistentUserId = 999L;
        NewItemRequest newItemRequest = createNewItemRequest("Молоток");

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemRequestService.create(nonExistentUserId, newItemRequest)
        );

        assertTrue(exception.getMessage().contains("Отсутствует пользователь по ID " + nonExistentUserId));

        long totalRequests = itemRequestRepository.count();
        assertEquals(0, totalRequests, "В БД не должно быть запросов после неудачного создания");
    }

    @Test
    void shouldSetCorrectCreatedTimestamp_Integration() {
        User user = createUser("user", "user@user.com");
        User userDb = userRepository.save(user);

        NewItemRequest newItemRequest = createNewItemRequest("Молоток");

        LocalDateTime beforeCreate = LocalDateTime.now();

        ItemRequestDto result = itemRequestService.create(userDb.getId(), newItemRequest);

        LocalDateTime afterCreate = LocalDateTime.now();

        assertNotNull(result.getCreated());
        assertTrue(result.getCreated().isAfter(beforeCreate.minusSeconds(1)),
                "created должен быть после времени до создания");
        assertTrue(result.getCreated().isBefore(afterCreate.plusSeconds(1)),
                "created должен быть до времени после создания");

        Optional<ItemRequest> dbRequest = itemRequestRepository.findById(result.getId());
        assertTrue(dbRequest.isPresent());
        assertNotNull(dbRequest.get().getCreated());
    }

    @Test
    void shouldGetAllItemRequestsByUserIdSuccessfully_Integration() {
        User user = createUser("user", "user@user.com");
        User userDb = userRepository.save(user);
        saveTwoItemRequestsForUserToDb(user);

        List<ItemRequestWithAnswersDto> result = itemRequestService.getAllByUserId(userDb.getId());

        assertNotNull(result);
        assertEquals(2, result.size());

        assertTrue(result.get(0).getCreated().isAfter(result.get(1).getCreated()),
                "Результаты должны быть отсортированы по created в порядке убывания");

        System.out.println(result);

        assertEquals("Рубанок", result.get(0).getDescription());
        assertEquals("Молоток", result.get(1).getDescription());
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoRequests_Integration() {
        User user = createUser("user", "user@user.com");
        User userDb = userRepository.save(user);
        Long userId = userDb.getId();

        List<ItemRequestWithAnswersDto> result = itemRequestService.getAllByUserId(userId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty(), "Должен возвращаться пустой список, если у пользователя нет запросов");
    }

    @Test
    void shouldThrowNotFoundExceptionWhenGetAllByUserDoesNotExist_Integration() {
        Long nonExistentUserId = 999L;

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemRequestService.getAllByUserId(nonExistentUserId)
        );

        assertTrue(exception.getMessage().contains("Отсутствует пользователь по ID " + nonExistentUserId),
                "Сообщение об ошибке должно содержать текст о не найденном пользователе");
    }

    @Test
    void shouldNotReturnRequestsFromOtherUsers_Integration() {
        User user1 = createUser("user", "user@user.com");
        User userDb = userRepository.save(user1);
        saveTwoItemRequestsForUserToDb(userDb);
        saveTwoUsersAndFourItemRequestsInDb();

        List<ItemRequestWithAnswersDto> result = itemRequestService.getAllByUserId(user1.getId());

        assertEquals(2, result.size());
        assertEquals("Рубанок", result.get(0).getDescription());
        assertEquals("Молоток", result.get(1).getDescription());
    }

    @Test
    void shouldGetAllRequestsFromOtherUsersSuccessfully_Integration() {
        User user = createUser("user", "user@user.com");
        User userDb = userRepository.save(user);
        saveTwoItemRequestsForUserToDb(userDb);
        saveTwoUsersAndFourItemRequestsInDb();

        List<ItemRequestDto> result = itemRequestService.getAllRequestsAnotherUsers(userDb.getId());

        assertNotNull(result);
        assertEquals(4, result.size(), "Должны вернуться запросы только от других пользователей");

        assertTrue(result.get(0).getCreated().isAfter(result.get(1).getCreated()) &&
                        result.get(1).getCreated().isAfter(result.get(2).getCreated()) &&
                        result.get(2).getCreated().isAfter(result.get(3).getCreated()),
                "Результаты должны быть отсортированы по created в порядке убывания");

        boolean containsUser1Request = result.stream()
                .anyMatch(dto -> dto.getDescription().equals("Молоток") ||
                        dto.getDescription().equals("Рубанок"));
        assertFalse(containsUser1Request, "В результате не должны быть запросы указанного пользователя");
    }

    @Test
    void shouldReturnEmptyListWhenNoOtherUsersHaveRequests_Integration() {
        User user = createUser("user", "user@user.com");
        User userDb = userRepository.save(user);

        User user2 = createUser("userSecond", "userSecond@user.com");
        userRepository.save(user2);

        List<ItemRequestDto> result = itemRequestService.getAllRequestsAnotherUsers(userDb.getId());

        assertNotNull(result);
        assertTrue(result.isEmpty(), "Должен возвращаться пустой список, если у других пользователей нет запросов");
    }

    @Test
    void shouldReturnAllRequestsWhenUserHasNoOwnRequests_Integration() {
        User user = createUser("user", "user@user.com");
        User userDb = userRepository.save(user);
        saveTwoUsersAndFourItemRequestsInDb();

        List<ItemRequestDto> result = itemRequestService.getAllRequestsAnotherUsers(userDb.getId());

        assertEquals(4, result.size());
        assertTrue(result.stream().anyMatch(dto -> dto.getDescription().equals("Долото")));
        assertTrue(result.stream().anyMatch(dto -> dto.getDescription().equals("Болгарка")));
        assertTrue(result.stream().anyMatch(dto -> dto.getDescription().equals("Рулетка")));
        assertTrue(result.stream().anyMatch(dto -> dto.getDescription().equals("Уровень")));
    }

    @Test
    void shouldFindItemRequestByIdSuccessfully_Integration() {
        User user = createUser("user", "user@user.com");
        User userDb = userRepository.save(user);

        NewItemRequest newItemRequest = createNewItemRequest("Молоток");

        ItemRequestDto createdRequest = itemRequestService.create(userDb.getId(), newItemRequest);

        ItemRequestWithAnswersDto result = itemRequestService.findById(createdRequest.getId());

        assertNotNull(result);
        assertEquals(createdRequest.getId(), result.getId());
        assertEquals("Молоток", result.getDescription());
        assertNotNull(result.getCreated());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenRequestDoesNotExist_Integration() {
        Long nonExistentRequestId = 999L;

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemRequestService.findById(nonExistentRequestId)
        );

        assertTrue(exception.getMessage().contains("не найден запрос по ID " + nonExistentRequestId),
                "Сообщение об ошибке должно содержать текст о не найденном запросе");

        Optional<ItemRequest> nonExistentRequest = itemRequestRepository.findById(nonExistentRequestId);
        assertFalse(nonExistentRequest.isPresent(),
                "В БД не должно быть запроса с указанным ID");
    }

    private User createUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return user;
    }

    private NewItemRequest createNewItemRequest(String itemName) {
        return NewItemRequest.builder().description(itemName).build();
    }

    private void saveTwoUsersAndFourItemRequestsInDb() {
        User user2 = createUser("userSecond", "userSecond@user.com");
        User user3 = createUser("userThird", "userThird@user.com");
        userRepository.save(user2);
        userRepository.save(user3);

        ItemRequest itemRequest3 = ItemRequestMapper.toItemRequest(createNewItemRequest("Долото"), user2);
        itemRequest3.setCreated(LocalDateTime.now().minusHours(8));
        itemRequestRepository.save(itemRequest3);

        ItemRequest itemRequest4 = ItemRequestMapper.toItemRequest(createNewItemRequest("Болгарка"), user2);
        itemRequest4.setCreated(LocalDateTime.now().minusHours(7));
        itemRequestRepository.save(itemRequest4);

        ItemRequest itemRequest5 = ItemRequestMapper.toItemRequest(createNewItemRequest("Рулетка"), user3);
        itemRequest5.setCreated(LocalDateTime.now().minusHours(6));
        itemRequestRepository.save(itemRequest5);

        ItemRequest itemRequest6 = ItemRequestMapper.toItemRequest(createNewItemRequest("Уровень"), user3);
        itemRequest6.setCreated(LocalDateTime.now().minusHours(5));
        itemRequestRepository.save(itemRequest6);
    }

    private void saveTwoItemRequestsForUserToDb(User user) {
        ItemRequest itemRequest1 = ItemRequestMapper.toItemRequest(createNewItemRequest("Молоток"), user);
        itemRequest1.setCreated(LocalDateTime.now().minusHours(3));
        itemRequestRepository.save(itemRequest1);

        ItemRequest itemRequest2 = ItemRequestMapper.toItemRequest(createNewItemRequest("Рубанок"), user);
        itemRequest2.setCreated(LocalDateTime.now().minusHours(1));
        itemRequestRepository.save(itemRequest2);
    }
}
