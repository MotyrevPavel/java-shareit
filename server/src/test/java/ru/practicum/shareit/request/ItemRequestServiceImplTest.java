package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithAnswersDto;
import ru.practicum.shareit.request.dto.NewItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    ItemRequestServiceImpl itemRequestService;

    @Test
    void shouldCreateItemRequestSuccessfully() {
        User user = new User();
        user.setId(1L);
        user.setName("Иван");
        user.setEmail("ivan@example.com");

        NewItemRequest newItemRequest = NewItemRequest.builder().description("Молоток").build();

        ItemRequest savedItemRequest = ItemRequestMapper.toItemRequest(newItemRequest, user);
        savedItemRequest.setId(100L);

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(itemRequestRepository.save(Mockito.any(ItemRequest.class))).thenReturn(savedItemRequest);

        ItemRequestDto result = itemRequestService.create(1L, newItemRequest);

        assertEquals(100L, result.getId());
        assertEquals("Молоток", result.getDescription());
        assertEquals(savedItemRequest.getCreated(), result.getCreated());

        Mockito.verify(userRepository, Mockito.times(1)).findById(1L);
        Mockito.verify(itemRequestRepository, Mockito.times(1))
                .save(Mockito.any(ItemRequest.class));
    }

    @Test
    void createShouldThrowNotFoundExceptionWhenUserDoesNotExist() {
        NewItemRequest newItemRequest = NewItemRequest.builder().description("Молоток").build();

        Mockito.when(userRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemRequestService.create(999L, newItemRequest)
        );

        assertTrue(exception.getMessage().contains("Отсутствует пользователь по ID"));

        Mockito.verify(itemRequestRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void shouldGetAllRequestsByUserIdSuccessfully() {
        User user = new User();
        user.setId(1L);

        ItemRequest request1 = createItemRequest(101L, LocalDateTime.now().minusHours(2), user);
        ItemRequest request2 = createItemRequest(102L, LocalDateTime.now().minusHours(1), user);

        List<ItemRequest> itemRequestList = List.of(request1, request2);

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(itemRequestRepository.findAllByUserId(1L,
                Sort.by(Sort.Direction.DESC, "created"))).thenReturn(itemRequestList);

        List<ItemRequestWithAnswersDto> result = itemRequestService.getAllByUserId(1L);

        System.out.println(result);

        assertEquals(2, result.size());
        assertEquals(101L, result.get(0).getId());
        assertEquals(102L, result.get(1).getId());
        assertTrue(result.get(0).getCreated().isBefore(result.get(1).getCreated()));

        Mockito.verify(userRepository, Mockito.times(1)).findById(1L);
        Mockito.verify(itemRequestRepository, Mockito.times(1))
                .findAllByUserId(Mockito.eq(1L), Mockito.any(Sort.class));
    }

    @Test
    void getAllByUserIdShouldThrowNotFoundExceptionWhenUserDoesNotExist() {
        Long userId = 999L;

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.empty());


        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemRequestService.getAllByUserId(userId)
        );

        assertTrue(exception.getMessage().contains("Отсутствует пользователь по ID"));

        Mockito.verify(userRepository, Mockito.times(1)).findById(userId);
        Mockito.verify(itemRequestRepository, Mockito.never()).findAllByUserId(Mockito.any(), Mockito.any());
    }

    @Test
    void shouldGetAllRequestsFromOtherUsersSuccessfully() {
        ItemRequest request1 = createItemRequest(101L, LocalDateTime.now().minusHours(2), new User());
        ItemRequest request2 = createItemRequest(102L, LocalDateTime.now().minusHours(1), new User());

        List<ItemRequest> itemRequestList = List.of(request1, request2);

        Mockito.when(itemRequestRepository.findAllByUser_IdNot(1L,
                        Sort.by(Sort.Direction.DESC, "created")))
                .thenReturn(itemRequestList);

        List<ItemRequestDto> result = itemRequestService.getAllRequestsAnotherUsers(1L);

        assertEquals(2, result.size());
        assertEquals(101L, result.get(0).getId());
        assertEquals(102L, result.get(1).getId());
        assertTrue(result.get(0).getCreated().isBefore(result.get(1).getCreated()));

        Mockito.verify(itemRequestRepository, Mockito.times(1))
                .findAllByUser_IdNot(Mockito.eq(1L), Mockito.any(Sort.class));
    }

    @Test
    void shouldReturnEmptyListWhenNoRequestsFromOtherUsers() {
        Mockito.when(itemRequestRepository.findAllByUser_IdNot(Mockito.eq(1L), Mockito.any(Sort.class)))
                .thenReturn(List.of());

        List<ItemRequestDto> result = itemRequestService.getAllRequestsAnotherUsers(1L);

        assertEquals(0, result.size());

        Mockito.verify(itemRequestRepository, Mockito.times(1))
                .findAllByUser_IdNot(Mockito.eq(1L), Mockito.any(Sort.class));
    }

    @Test
    void shouldFindItemRequestByIdSuccessfully() {
        ItemRequest itemRequest = createItemRequest(101L, LocalDateTime.now(), new User());

        Mockito.when(itemRequestRepository.findById(101L)).thenReturn(Optional.of(itemRequest));

        ItemRequestWithAnswersDto result = itemRequestService.findById(101L);

        assertEquals(101L, result.getId());
        assertEquals("Запрос 101", result.getDescription());
        assertEquals(itemRequest.getCreated(), result.getCreated());

        Mockito.verify(itemRequestRepository, Mockito.times(1)).findById(101L);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenRequestNotFound() {

        Mockito.when(itemRequestRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemRequestService.findById(999L)
        );

        assertTrue(exception.getMessage().contains("не найден запрос по ID "));

        Mockito.verify(itemRequestRepository).findById(999L);
    }

    private ItemRequest createItemRequest(Long id, LocalDateTime time, User user) {
        ItemRequest request = new ItemRequest();
        request.setId(id);
        request.setDescription("Запрос " + id);
        request.setCreated(time);
        request.setUser(user);
        request.setItems(new ArrayList<>());
        return request;
    }
}