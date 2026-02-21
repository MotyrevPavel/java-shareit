package ru.practicum.shareit.request;

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
import ru.practicum.shareit.request.dto.NewItemRequest;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemRequestClient itemRequestClient;

    @Test
    void shouldReturnCreatedWhenCreateRequestWithValidData() throws Exception {
        Long userId = 1L;
        NewItemRequest newItemRequest = new NewItemRequest();
        newItemRequest.setDescription("Valid description");

        ResponseEntity<Object> expectedResponse = ResponseEntity.status(201).body("Request created");
        when(itemRequestClient.create(any(NewItemRequest.class), eq(userId))).thenReturn(expectedResponse);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newItemRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Request created"));

        verify(itemRequestClient, times(1)).create(any(NewItemRequest.class), eq(userId));
    }

    @Test
    void shouldReturnBadRequestWhenCreateRequestWithNullDescription() throws Exception {
        Long userId = 1L;
        NewItemRequest newItemRequest = new NewItemRequest();
        newItemRequest.setDescription(null);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newItemRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(
                        MethodArgumentNotValidException.class,
                        result.getResolvedException()));
    }

    @Test
    void shouldReturnBadRequestWhenCreateRequestWithEmptyDescription() throws Exception {
        Long userId = 1L;
        NewItemRequest newItemRequest = new NewItemRequest();
        newItemRequest.setDescription("");

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newItemRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenCreateRequestWithBlankDescription() throws Exception {
        Long userId = 1L;
        NewItemRequest newItemRequest = new NewItemRequest();
        newItemRequest.setDescription("   ");

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newItemRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenCreateRequestWithoutUserIdHeader() throws Exception {
        NewItemRequest newItemRequest = new NewItemRequest();
        newItemRequest.setDescription("Description");

        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newItemRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnOkWhenGetRequestsByUserId() throws Exception {
        Long userId = 1L;
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().body("Requests list");
        when(itemRequestClient.getAllByUserId(userId)).thenReturn(expectedResponse);

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().string("Requests list"));

        verify(itemRequestClient, times(1)).getAllByUserId(userId);
    }

    @Test
    void shouldReturnBadRequestWhenGetRequestsWithoutUserIdHeader() throws Exception {
        mockMvc.perform(get("/requests"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnOkWhenGetAllRequests() throws Exception {
        Long userId = 1L;
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().body("All requests");
        when(itemRequestClient.getAllRequestsAnotherUsers(userId)).thenReturn(expectedResponse);

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().string("All requests"));

        verify(itemRequestClient, times(1)).getAllRequestsAnotherUsers(userId);
    }

    @Test
    void shouldReturnBadRequestWhenGetAllRequestsWithoutUserIdHeader() throws Exception {
        mockMvc.perform(get("/requests/all"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnOkWhenGetRequestById() throws Exception {
        Long requestId = 1L;
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().body("Request data");
        when(itemRequestClient.findById(requestId)).thenReturn(expectedResponse);

        mockMvc.perform(get("/requests/{requestId}", requestId))
                .andExpect(status().isOk())
                .andExpect(content().string("Request data"));

        verify(itemRequestClient, times(1)).findById(requestId);
    }

    @Test
    void shouldReturnBadRequestWhenGetRequestWithNonNumericId() throws Exception {
        String invalidId = "abc";

        mockMvc.perform(get("/requests/{requestId}", invalidId))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(
                        MethodArgumentTypeMismatchException.class,
                        result.getResolvedException()));
    }
}
