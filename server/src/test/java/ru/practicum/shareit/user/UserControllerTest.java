package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.NewUser;
import ru.practicum.shareit.user.dto.UpdateUser;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private UserService userService;

    @Test
    void shouldCreateUserSuccessfully() throws Exception {
        NewUser newUser = NewUser.builder()
                .name("NewUser")
                .email("NewUser@example.com")
                .build();

        UserDto expectedUserDto = UserDto.builder()
                .id(1L)
                .name("NewUser")
                .email("NewUser@example.com")
                .build();

        when(userService.create(any(NewUser.class))).thenReturn(expectedUserDto);

        mockMvc.perform(post("/users")
                        .content(mapper.writeValueAsString(newUser))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("NewUser"))
                .andExpect(jsonPath("$.email").value("NewUser@example.com"));
    }

    @Test
    void shouldGetUserByIdSuccessfully() throws Exception {
        Long userId = 1L;
        UserDto expectedUserDto = UserDto.builder()
                .id(userId)
                .name("Alice")
                .email("alice@example.com")
                .build();

        when(userService.getById(userId)).thenReturn(expectedUserDto);

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    void shouldUpdateUserSuccessfully() throws Exception {
        Long userId = 1L;
        UpdateUser updateUser = UpdateUser.builder()
                .name("Updated Name")
                .email("updated@example.com")
                .build();

        UserDto updatedUserDto = UserDto.builder()
                .id(userId)
                .name("Updated Name")
                .email("updated@example.com")
                .build();

        when(userService.update(any(UpdateUser.class), anyLong())).thenReturn(updatedUserDto);

        mockMvc.perform(patch("/users/{id}", userId)
                        .content(mapper.writeValueAsString(updateUser))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }

    @Test
    void shouldDeleteUserSuccessfully() throws Exception {
        Long userId = 1L;

        doNothing().when(userService).delete(userId);

        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isOk());
    }
}