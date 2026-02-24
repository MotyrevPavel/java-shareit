package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithAnswersDto;
import ru.practicum.shareit.request.dto.NewItemRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemRequestService itemRequestService;

    @Test
    void shouldCreateRequestSuccessfully() throws Exception {
        Long userId = 1L;
        NewItemRequest newItemRequest = NewItemRequest.builder()
                .description("Need a tool for home repairs")
                .build();

        ItemRequestDto expectedResponse = ItemRequestDto.builder()
                .id(1L)
                .description("Need a tool for home repairs")
                .created(LocalDateTime.now())
                .build();

        when(itemRequestService.create(userId, newItemRequest))
                .thenReturn(expectedResponse);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newItemRequest)))
                .andExpect(status().isOk()) // Сначала проверяем статус
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Need a tool for home repairs"))
                .andExpect(jsonPath("$.created").isNotEmpty());
    }

    @Test
    void shouldGetRequestsSuccessfully() throws Exception {
        Long userId = 1L;
        LocalDateTime now = LocalDateTime.now();

        ItemRequestWithAnswersDto dto1 = ItemRequestWithAnswersDto.builder()
                .id(1L)
                .description("Описание запроса 1")
                .created(now)
                .items(List.of())
                .build();

        ItemRequestWithAnswersDto dto2 = ItemRequestWithAnswersDto.builder()
                .id(2L)
                .description("Описание запроса 2")
                .created(now.plusHours(1))
                .items(List.of())
                .build();

        List<ItemRequestWithAnswersDto> expectedList = List.of(dto1, dto2);

        when(itemRequestService.getAllByUserId(userId)).thenReturn(expectedList);

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].description").value("Описание запроса 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].description").value("Описание запроса 2"));
    }

    @Test
    void shouldGetAllRequestsSuccessfully() throws Exception {
        Long userId = 1L;
        LocalDateTime now = LocalDateTime.now();

        ItemRequestDto dto1 = ItemRequestDto.builder()
                .id(1L)
                .description("Запрос 1")
                .created(now)
                .build();

        ItemRequestDto dto2 = ItemRequestDto.builder()
                .id(2L)
                .description("Запрос 2")
                .created(now.plusHours(1))
                .build();

        List<ItemRequestDto> expectedList = List.of(dto1, dto2);

        when(itemRequestService.getAllRequestsAnotherUsers(userId)).thenReturn(expectedList);

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].description").value("Запрос 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].description").value("Запрос 2"))
                .andExpect(jsonPath("$[0].created").exists())
                .andExpect(jsonPath("$[1].created").exists());
    }

    @Test
    void shouldGetRequestSuccessfully() throws Exception {
        Long requestId = 1L;
        LocalDateTime createdTime = LocalDateTime.now();

        ItemRequestWithAnswersDto expectedDto = ItemRequestWithAnswersDto.builder()
                .id(requestId)
                .description("Описание тестового запроса")
                .created(createdTime)
                .items(List.of())
                .build();

        when(itemRequestService.findById(requestId)).thenReturn(expectedDto);

        mockMvc.perform(get("/requests/{requestId}", requestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestId))
                .andExpect(jsonPath("$.description").value("Описание тестового запроса"))
                .andExpect(jsonPath("$.created").exists())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(0));
    }
}
