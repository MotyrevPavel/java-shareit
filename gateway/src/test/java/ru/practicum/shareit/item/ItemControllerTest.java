package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.shareit.item.comment.dto.NewComment;
import ru.practicum.shareit.item.dto.NewItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemClient itemClient;

    @Test
    void shouldReturnCreatedWhenCreateItemWithValidData() throws Exception {
        Long userId = 1L;
        NewItemDto itemDto = new NewItemDto();
        itemDto.setName("Drill");
        itemDto.setDescription("Powerful electric drill");
        itemDto.setAvailable(true);

        ResponseEntity<Object> expectedResponse = ResponseEntity.status(201).body("Item created");
        when(itemClient.create(any(NewItemDto.class), eq(userId))).thenReturn(expectedResponse);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Item created"));

        verify(itemClient, times(1)).create(any(NewItemDto.class), eq(userId));
    }

    @Test
    void shouldReturnBadRequestWhenCreateItemWithNullName() throws Exception {
        Long userId = 1L;
        NewItemDto itemDto = new NewItemDto();
        itemDto.setName(null);
        itemDto.setDescription("Description");
        itemDto.setAvailable(true);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(
                        MethodArgumentNotValidException.class,
                        result.getResolvedException()));
    }

    @Test
    void shouldReturnBadRequestWhenCreateItemWithEmptyName() throws Exception {
        Long userId = 1L;
        NewItemDto itemDto = new NewItemDto();
        itemDto.setName("");
        itemDto.setDescription("Description");
        itemDto.setAvailable(true);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenCreateItemWithBlankName() throws Exception {
        Long userId = 1L;
        NewItemDto itemDto = new NewItemDto();
        itemDto.setName("   ");
        itemDto.setDescription("Description");
        itemDto.setAvailable(true);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenCreateItemWithNullDescription() throws Exception {
        Long userId = 1L;
        NewItemDto itemDto = new NewItemDto();
        itemDto.setName("Item");
        itemDto.setDescription(null);
        itemDto.setAvailable(true);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenCreateItemWithEmptyDescription() throws Exception {
        Long userId = 1L;
        NewItemDto itemDto = new NewItemDto();
        itemDto.setName("Item");
        itemDto.setDescription("");
        itemDto.setAvailable(true);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenCreateItemWithBlankDescription() throws Exception {
        Long userId = 1L;
        NewItemDto itemDto = new NewItemDto();
        itemDto.setName("Item");
        itemDto.setDescription("    ");
        itemDto.setAvailable(true);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenCreateItemWithNullAvailable() throws Exception {
        Long userId = 1L;
        NewItemDto itemDto = new NewItemDto();
        itemDto.setName("Drill");
        itemDto.setDescription("Powerful electric drill");
        itemDto.setAvailable(null);

        ResponseEntity<Object> expectedResponse = ResponseEntity.status(201).body("Item created");
        when(itemClient.create(any(NewItemDto.class), eq(userId))).thenReturn(expectedResponse);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenCreateItemWithoutUserIdHeader() throws Exception {
        NewItemDto itemDto = new NewItemDto();
        itemDto.setName("Item");
        itemDto.setDescription("Description");
        itemDto.setAvailable(true);

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnOkWhenUpdateItemWithValidData() throws Exception {
        Long itemId = 1L;
        Long userId = 1L;
        UpdateItemDto updateItemDto = new UpdateItemDto();
        updateItemDto.setName("Updated Name");
        updateItemDto.setDescription("Updated Description");
        updateItemDto.setAvailable(false);

        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().body("Item updated");
        when(itemClient.update(any(UpdateItemDto.class), eq(itemId), eq(userId)))
                .thenReturn(expectedResponse);

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateItemDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Item updated"));

        verify(itemClient, times(1)).update(any(UpdateItemDto.class), eq(itemId), eq(userId));
    }

    @Test
    void shouldReturnOkWhenUpdateOnlyName() throws Exception {
        Long itemId = 1L;
        Long userId = 1L;
        UpdateItemDto updateItemDto = new UpdateItemDto();
        updateItemDto.setName("New Name");

        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().body("Item updated");
        when(itemClient.update(any(UpdateItemDto.class), eq(itemId), eq(userId)))
                .thenReturn(expectedResponse);

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateItemDto)))
                .andExpect(status().isOk());

        verify(itemClient, times(1)).update(any(UpdateItemDto.class), eq(itemId), eq(userId));
    }

    @Test
    void shouldReturnBadRequestWhenUpdateWithBlankName() throws Exception {
        Long itemId = 1L;
        Long userId = 1L;
        UpdateItemDto updateItemDto = new UpdateItemDto();
        updateItemDto.setName("   ");

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateItemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenUpdateWithEmptyName() throws Exception {
        Long itemId = 1L;
        Long userId = 1L;
        UpdateItemDto updateItemDto = new UpdateItemDto();
        updateItemDto.setName("");

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateItemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnOkWhenUpdateOnlyDescription() throws Exception {
        Long itemId = 1L;
        Long userId = 1L;
        UpdateItemDto updateItemDto = new UpdateItemDto();
        updateItemDto.setDescription("New Description");

        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().body("Item updated");
        when(itemClient.update(any(UpdateItemDto.class), eq(itemId), eq(userId)))
                .thenReturn(expectedResponse);

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateItemDto)))
                .andExpect(status().isOk());

        verify(itemClient, times(1)).update(any(UpdateItemDto.class), eq(itemId), eq(userId));
    }

    @Test
    void shouldReturnBadRequestWhenUpdateWithBlankDescription() throws Exception {
        Long itemId = 1L;
        Long userId = 1L;
        UpdateItemDto updateItemDto = new UpdateItemDto();
        updateItemDto.setDescription("      ");

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateItemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenUpdateWithEmptyDescription() throws Exception {
        Long itemId = 1L;
        Long userId = 1L;
        UpdateItemDto updateItemDto = new UpdateItemDto();
        updateItemDto.setDescription("");

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateItemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnOkWhenUpdateOnlyAvailable() throws Exception {
        Long itemId = 1L;
        Long userId = 1L;
        UpdateItemDto updateItemDto = new UpdateItemDto();
        updateItemDto.setAvailable(false);

        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().body("Item updated");
        when(itemClient.update(any(UpdateItemDto.class), eq(itemId), eq(userId)))
                .thenReturn(expectedResponse);

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateItemDto)))
                .andExpect(status().isOk());

        verify(itemClient, times(1)).update(any(UpdateItemDto.class), eq(itemId), eq(userId));
    }

    @Test
    void shouldReturnOkWhenGetItemById() throws Exception {
        Long itemId = 1L;
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().body("Item data");
        when(itemClient.getById(itemId)).thenReturn(expectedResponse);

        mockMvc.perform(get("/items/{itemId}", itemId))
                .andExpect(status().isOk())
                .andExpect(content().string("Item data"));

        verify(itemClient, times(1)).getById(itemId);
    }

    @Test
    void shouldReturnBadRequestWhenGetItemWithNonNumericId() throws Exception {
        String invalidId = "abc";

        mockMvc.perform(get("/items/{itemId}", invalidId))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(
                        MethodArgumentTypeMismatchException.class,
                        result.getResolvedException()));
    }

    @Test
    void shouldReturnOkWhenGetItemsByUserId() throws Exception {
        Long userId = 1L;
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().body("Items list");
        when(itemClient.getByItemsByUserId(userId)).thenReturn(expectedResponse);

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().string("Items list"));

        verify(itemClient, times(1)).getByItemsByUserId(userId);
    }

    @Test
    void shouldReturnBadRequestWhenGetItemsWithoutUserIdHeader() throws Exception {
        mockMvc.perform(get("/items"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void shouldReturnOkWhenSearchItemsWithValidText() throws Exception {
        String searchText = "drill";
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().body("Search results");
        when(itemClient.searchAvailableItemByParam(searchText)).thenReturn(expectedResponse);

        mockMvc.perform(get("/items/search")
                        .param("text", searchText))
                .andExpect(status().isOk())
                .andExpect(content().string("Search results"));

        verify(itemClient, times(1)).searchAvailableItemByParam(searchText);
    }

    @Test
    void shouldReturnBadRequestWhenSearchWithoutTextParam() throws Exception {
        mockMvc.perform(get("/items/search"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnCreatedWhenCreateCommentWithValidData() throws Exception {
        Long itemId = 1L;
        Long userId = 1L;
        NewComment comment = new NewComment();
        comment.setText("Great item, very useful!");

        ResponseEntity<Object> expectedResponse = ResponseEntity.status(201).body("Comment created");
        when(itemClient.createNewComment(eq(itemId), eq(userId), any(NewComment.class)))
                .thenReturn(expectedResponse);

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(comment)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Comment created"));

        verify(itemClient, times(1)).createNewComment(eq(itemId), eq(userId), any(NewComment.class));
    }

    @Test
    void shouldReturnBadRequestWhenCreateCommentWithNullText() throws Exception {
        Long itemId = 1L;
        Long userId = 1L;
        NewComment comment = new NewComment();

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(comment)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(
                        MethodArgumentNotValidException.class,
                        result.getResolvedException()));
    }

    @Test
    void shouldReturnBadRequestWhenCreateCommentWithEmptyText() throws Exception {
        Long itemId = 1L;
        Long userId = 1L;
        NewComment comment = new NewComment();
        comment.setText("");

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(comment)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenCreateCommentWithBlankText() throws Exception {
        Long itemId = 1L;
        Long userId = 1L;
        NewComment comment = new NewComment();
        comment.setText("   ");

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(comment)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenCreateCommentWithoutUserIdHeader() throws Exception {
        Long itemId = 1L;
        NewComment comment = new NewComment();
        comment.setText("Valid comment text");

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(comment)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenCreateCommentWithNonNumericItemId() throws Exception {
        String invalidItemId = "abc";
        Long userId = 1L;
        NewComment comment = new NewComment();
        comment.setText("Valid comment text");

        mockMvc.perform(post("/items/{itemId}/comment", invalidItemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(comment)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(
                        MethodArgumentTypeMismatchException.class,
                        result.getResolvedException()));
    }
}