package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.dto.NewComment;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemFullDto;
import ru.practicum.shareit.item.dto.NewItem;
import ru.practicum.shareit.item.dto.UpdateItem;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    ItemService itemService;

    @Test
    void shouldReturnCreatedItemSuccessfully() throws Exception {
        NewItem newItem = NewItem.builder()
                .name("Test Item")
                .description("Test description")
                .available(true)
                .requestId(null)
                .build();

        ItemDto expectedDto = ItemDto.builder()
                .id(1L)
                .name("Test Item")
                .description("Test description")
                .available(true)
                .userId(100L)
                .build();

        when(itemService.create(any(NewItem.class), any(Long.class)))
                .thenReturn(expectedDto);

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 100L)
                        .content(mapper.writeValueAsString(newItem)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Item"))
                .andExpect(jsonPath("$.description").value("Test description"))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.userId").value(100L));
    }

    @Test
    void shouldUpdateItemSuccessfully() throws Exception {
        UpdateItem updateItem = UpdateItem.builder()
                .name("Updated Item")
                .description("Updated description")
                .available(false)
                .build();

        ItemDto expectedDto = ItemDto.builder()
                .id(1L)
                .name("Updated Item")
                .description("Updated description")
                .available(false)
                .userId(100L)
                .build();

        when(itemService.update(any(UpdateItem.class), any(Long.class), any(Long.class)))
                .thenReturn(expectedDto);

        mockMvc.perform(patch("/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 100L)
                        .content(mapper.writeValueAsString(updateItem)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Updated Item"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.available").value(false))
                .andExpect(jsonPath("$.userId").value(100L));
    }

    @Test
    void shouldGetItemByIdSuccessfully() throws Exception {
        ItemFullDto expectedDto = ItemFullDto.builder()
                .id(1L)
                .name("Test Item")
                .description("Test description")
                .available(true)
                .userId(100L)
                .nextBooking(null)
                .lastBooking(null)
                .comments(List.of())
                .build();

        when(itemService.getById(any(Long.class))).thenReturn(expectedDto);

        mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Item"))
                .andExpect(jsonPath("$.description").value("Test description"))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.userId").value(100L))
                .andExpect(jsonPath("$.nextBooking").doesNotExist())
                .andExpect(jsonPath("$.lastBooking").doesNotExist())
                .andExpect(jsonPath("$.comments").isArray())
                .andExpect(jsonPath("$.comments.length()").value(0));
    }

    @Test
    void shouldGetAllItemsByUserIdSuccessfully() throws Exception {
        List<ItemFullDto> expectedItems = List.of(
                ItemFullDto.builder()
                        .id(1L)
                        .name("Item 1")
                        .description("Description 1")
                        .available(true)
                        .userId(100L)
                        .nextBooking(null)
                        .lastBooking(null)
                        .comments(List.of())
                        .build(),
                ItemFullDto.builder()
                        .id(2L)
                        .name("Item 2")
                        .description("Description 2")
                        .available(false)
                        .userId(100L)
                        .nextBooking(null)
                        .lastBooking(null)
                        .comments(List.of())
                        .build()
        );

        when(itemService.getAllByUserId(any(Long.class))).thenReturn(expectedItems);

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 100L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Item 1"))
                .andExpect(jsonPath("$[0].available").value(true))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Item 2"))
                .andExpect(jsonPath("$[1].available").value(false));
    }

    @Test
    void shouldSearchItemsSuccessfully() throws Exception {
        List<ItemDto> expectedItems = List.of(
                ItemDto.builder()
                        .id(1L)
                        .name("Search Item 1")
                        .description("Description with search term")
                        .available(true)
                        .userId(100L)
                        .build(),
                ItemDto.builder()
                        .id(2L)
                        .name("Search Item 2")
                        .description("Another description with search term")
                        .available(false)
                        .userId(200L)
                        .build()
        );

        when(itemService.searchAvailableItemByParam(any(String.class))).thenReturn(expectedItems);

        mockMvc.perform(get("/items/search")
                        .param("text", "search term"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Search Item 1"))
                .andExpect(jsonPath("$[0].available").value(true))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Search Item 2"))
                .andExpect(jsonPath("$[1].available").value(false));
    }

    @Test
    void shouldCreateCommentSuccessfully() throws Exception {
        NewComment newComment = NewComment.builder()
                .text("Great item!")
                .build();

        CommentDto expectedCommentDto = CommentDto.builder()
                .id(1L)
                .text("Great item!")
                .authorName("Test User")
                .created(LocalDateTime.now())
                .build();

        when(itemService.createNewComment(any(Long.class), any(Long.class), any(NewComment.class)))
                .thenReturn(expectedCommentDto);

        mockMvc.perform(post("/items/1/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 100L)
                        .content(mapper.writeValueAsString(newComment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.text").value("Great item!"))
                .andExpect(jsonPath("$.authorName").value("Test User"));
    }
}