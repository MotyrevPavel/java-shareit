package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.NewUser;
import ru.practicum.shareit.user.dto.UpdateUser;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void shouldCreateUserSuccessfully() {
        NewUser newUserDto = createNewUserAlice();
        User newUser = UserMapper.toUser(newUserDto);

        User user = createUserALice();

        Mockito.when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(user);

        UserDto result = userService.create(newUserDto);

        assertEquals("Alice", result.getName());
        assertEquals("alice@example.com", result.getEmail());
        assertEquals(1L, result.getId());

        Mockito.verify(userRepository, Mockito.times(1)).existsByEmail("alice@example.com");
        Mockito.verify(userRepository, Mockito.times(1)).save(newUser);
    }

    @Test
    void shouldThrowValidationExceptionWhenSaveUserWithExistEmail() {
        NewUser newUser = createNewUserAlice();

        Mockito.when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userService.create(newUser)
        );

        assertTrue(exception.getMessage().contains("Данный email уже занят"));
        assertTrue(exception.getMessage().contains("alice@example.com"));

        Mockito.verify(userRepository, Mockito.times(1)).existsByEmail("alice@example.com");
        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any(User.class));
    }

    @Test
    void shouldReturnUserByIdSuccessfully() {
        User user = createUserALice();

        Mockito.when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(user));

        UserDto userDto = userService.getById(1L);

        assertEquals(1L, userDto.getId());
        assertEquals("Alice", userDto.getName());
        assertEquals("alice@example.com", userDto.getEmail());

        Mockito.verify(userRepository, Mockito.times(1)).findById(1L);
    }

    @Test
    void shouldUpdateUserSuccessfully() {
        UpdateUser updateUser = UpdateUser.builder().name("New Name").email("new@example.com").build();

        User existingUser = createUserALice();

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setName("New Name");
        updatedUser.setEmail("new@example.com");

        Mockito.when(userRepository.existsByEmail(updateUser.getEmail())).thenReturn(false);
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(updatedUser);

        UserDto result = userService.update(updateUser, 1L);

        assertEquals("New Name", result.getName());
        assertEquals("new@example.com", result.getEmail());
        assertEquals(1L, result.getId());

        Mockito.verify(userRepository, Mockito.times(1)).existsByEmail(updateUser.getEmail());
        Mockito.verify(userRepository, Mockito.times(1)).findById(1L);
        Mockito.verify(userRepository, Mockito.times(1)).save(updatedUser);
    }

    @Test
    void shouldUpdateUserOnlyWithNameSuccessfully() {
        UpdateUser updateUser = UpdateUser.builder().name("New Name").build();

        User existingUser = createUserALice();

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setName("New Name");
        updatedUser.setEmail("alice@example.com");

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(updatedUser);

        UserDto result = userService.update(updateUser, 1L);

        assertEquals("New Name", result.getName());
        assertEquals("alice@example.com", result.getEmail());
        assertEquals(1L, result.getId());

        Mockito.verify(userRepository, Mockito.never()).existsByEmail(updateUser.getEmail());
        Mockito.verify(userRepository, Mockito.times(1)).findById(1L);
        Mockito.verify(userRepository, Mockito.times(1)).save(updatedUser);
    }

    @Test
    void shouldUpdateUserOnlyWithEmailSuccessfully() {
        UpdateUser updateUser = UpdateUser.builder().email("new@example.com").build();

        User existingUser = createUserALice();

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setName("Alice");
        updatedUser.setEmail("new@example.com");

        Mockito.when(userRepository.existsByEmail(updateUser.getEmail())).thenReturn(false);
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(updatedUser);

        UserDto result = userService.update(updateUser, 1L);

        assertEquals("Alice", result.getName());
        assertEquals("new@example.com", result.getEmail());
        assertEquals(1L, result.getId());

        Mockito.verify(userRepository, Mockito.times(1)).existsByEmail(updateUser.getEmail());
        Mockito.verify(userRepository, Mockito.times(1)).findById(1L);
        Mockito.verify(userRepository, Mockito.times(1)).save(updatedUser);
    }

    @Test
    void shouldThrowValidationExceptionWhenEmailExists() {
        UpdateUser updateUser = UpdateUser.builder().email("existing@example.com").build();

        Mockito.when(userRepository.existsByEmail(updateUser.getEmail())).thenReturn(true);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userService.update(updateUser, 1L)
        );

        assertTrue(exception.getMessage().contains("Данный email уже есть в базе"));

        Mockito.verify(userRepository, Mockito.never()).findById(Mockito.anyLong());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUserNotFound() {
        UpdateUser updateUser = UpdateUser.builder().name("New Name").email("new@example.com").build();

        Mockito.when(userRepository.existsByEmail(updateUser.getEmail())).thenReturn(false);
        Mockito.when(userRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.update(updateUser, 999L)
        );

        assertTrue(exception.getMessage().contains("Пользователь не найден по ID"));

        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any(User.class));
    }

    @Test
    void shouldDeleteUserSuccessfully() {
        userService.delete(1L);

        Mockito.verify(userRepository, Mockito.times(1)).deleteById(1L);
    }

    private User createUserALice() {
        User user = new User();
        user.setId(1L);
        user.setName("Alice");
        user.setEmail("alice@example.com");
        return user;
    }

    private NewUser createNewUserAlice() {
        return NewUser.builder().name("Alice").email("alice@example.com").build();
    }
}