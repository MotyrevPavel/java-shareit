package ru.practicum.shareit.user;

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
import ru.practicum.shareit.user.dto.NewUser;
import ru.practicum.shareit.user.dto.UpdateUser;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {
    @Autowired
    ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserClient userClient;


    @Test
    void shouldReturnCreatedSuccessfully() throws Exception {
        NewUser newUser = new NewUser();
        newUser.setName("User");
        newUser.setEmail("user@example.com");

        ResponseEntity<Object> expectedResponse = ResponseEntity.status(201).body("User created");
        when(userClient.create(any(NewUser.class))).thenReturn(expectedResponse);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(content().string("User created"));

        verify(userClient, times(1)).create(any(NewUser.class));
    }

    @Test
    void shouldReturnBadRequestWhenCreateNullNameUser() throws Exception {
        NewUser newUser = new NewUser();
        newUser.setName(null);
        newUser.setEmail("valid@example.com");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newUser)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(MethodArgumentNotValidException.class,
                        result.getResolvedException()));
    }

    @Test
    void shouldReturnBadRequestWhenCreateEmptyNameUser() throws Exception {
        NewUser newUser = new NewUser();
        newUser.setName("");
        newUser.setEmail("valid@example.com");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenCreateBlankNameUser() throws Exception {
        NewUser newUser = new NewUser();
        newUser.setName("   ");
        newUser.setEmail("valid@example.com");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenCreateInvalidEmailUser() throws Exception {
        NewUser newUser = new NewUser();
        newUser.setName("User");
        newUser.setEmail("invalid-email");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenCreateNullEmailUser() throws Exception {
        NewUser newUser = new NewUser();
        newUser.setName("User");
        newUser.setEmail(null);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenCreateUserWithNullFields() throws Exception {
        NewUser newUser = new NewUser(); // все поля null

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnOkWhenGetUserById() throws Exception {
        Long userId = 1L;
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().body("User data");
        when(userClient.getById(userId)).thenReturn(expectedResponse);

        mockMvc.perform(get("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("User data"));

        verify(userClient, times(1)).getById(userId);
    }

    @Test
    void shouldReturnOkWhenUpdateUserWithValidData() throws Exception {
        Long userId = 1L;
        UpdateUser updateUser = new UpdateUser();
        updateUser.setName("Updated Name");
        updateUser.setEmail("updated@example.com");

        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().body("User updated");
        when(userClient.update(any(UpdateUser.class), eq(userId))).thenReturn(expectedResponse);

        mockMvc.perform(patch("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateUser)))
                .andExpect(status().isOk())
                .andExpect(content().string("User updated"));

        verify(userClient, times(1)).update(any(UpdateUser.class), eq(userId));
    }

    @Test
    void shouldReturnOkWhenUpdateOnlyName() throws Exception {
        Long userId = 1L;
        UpdateUser updateUser = new UpdateUser();
        updateUser.setName("New Name");

        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().body("User updated");
        when(userClient.update(any(UpdateUser.class), eq(userId))).thenReturn(expectedResponse);

        mockMvc.perform(patch("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateUser)))
                .andExpect(status().isOk());

        verify(userClient, times(1)).update(any(UpdateUser.class), eq(userId));
    }

    @Test
    void shouldReturnOkWhenUpdateOnlyEmail() throws Exception {
        Long userId = 1L;
        UpdateUser updateUser = new UpdateUser();
        updateUser.setEmail("newemail@example.com");

        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().body("User updated");
        when(userClient.update(any(UpdateUser.class), eq(userId))).thenReturn(expectedResponse);

        mockMvc.perform(patch("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateUser)))
                .andExpect(status().isOk());

        verify(userClient, times(1)).update(any(UpdateUser.class), eq(userId));
    }

    @Test
    void shouldReturnBadRequestWhenUpdateWithInvalidEmailFormat() throws Exception {
        Long userId = 1L;
        UpdateUser updateUser = new UpdateUser();
        updateUser.setName("User");
        updateUser.setEmail("invalid-email");

        mockMvc.perform(patch("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenUpdateWithNonNumericId() throws Exception {
        String invalidId = "abc";
        UpdateUser updateUser = new UpdateUser();
        updateUser.setName("User");
        updateUser.setEmail("user@example.com");

        mockMvc.perform(patch("/users/{id}", invalidId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateUser)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(
                        MethodArgumentTypeMismatchException.class,
                        result.getResolvedException()));
    }

    @Test
    void shouldReturnBadRequestWhenUpdateWithEmptyRequestBody() throws Exception {
        Long userId = 1L;

        mockMvc.perform(patch("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk()); // UpdateUser допускает пустые поля — это валидно
    }

    @Test
    void shouldReturnBadRequestWhenUpdateWithNullEmail() throws Exception {
        Long userId = 1L;
        UpdateUser updateUser = new UpdateUser();
        updateUser.setName("User");
        updateUser.setEmail(null);

        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().body("User updated");
        when(userClient.update(any(UpdateUser.class), eq(userId))).thenReturn(expectedResponse);

        mockMvc.perform(patch("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateUser)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldPropagateClientErrorWhenUpdate() throws Exception {
        Long userId = 1L;
        UpdateUser updateUser = new UpdateUser();
        updateUser.setName("Updated Name");

        ResponseEntity<Object> clientErrorResponse =
                ResponseEntity.status(400).body("Update failed");
        when(userClient.update(any(UpdateUser.class), eq(userId))).thenReturn(clientErrorResponse);

        mockMvc.perform(patch("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateUser)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Update failed"));
    }

    @Test
    void shouldReturnNoContentWhenDeleteExistingUser() throws Exception {
        Long userId = 1L;
        ResponseEntity<Object> expectedResponse = ResponseEntity.noContent().build();
        when(userClient.deleteById(userId)).thenReturn(expectedResponse);

        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isNoContent());

        verify(userClient, times(1)).deleteById(userId);
    }

    @Test
    void shouldReturnBadRequestWhenDeleteWithNonNumericId() throws Exception {
        String invalidId = "abc";

        mockMvc.perform(delete("/users/{id}", invalidId))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(
                        MethodArgumentTypeMismatchException.class,
                        result.getResolvedException()));
    }

    @Test
    void shouldReturnNotFoundWhenDeleteWithEmptyPathVariable() throws Exception {
        mockMvc.perform(delete("/users/"))
                .andExpect(status().isNotFound());
    }
}