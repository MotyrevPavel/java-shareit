package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.NewUser;
import ru.practicum.shareit.user.dto.UpdateUser;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@ExtendWith(SpringExtension.class)
public class UserServiceImplIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldCreateUserSuccessfully_Integration() {
        NewUser newUser = createNewUser();

        UserDto result = userService.create(newUser);

        assertNotNull(result.getId());
        assertEquals(newUser.getName(), result.getName());
        assertEquals(newUser.getEmail(), result.getEmail());

        Optional<User> savedUser = userRepository.findById(result.getId());
        assertTrue(savedUser.isPresent());
        assertEquals("Alice", savedUser.get().getName());
    }

    @Test
    void shouldReturnUserByIdSuccessfully_Integration() {
        NewUser newUser = createNewUser();

        UserDto createdUser = userService.create(newUser);
        Long userId = createdUser.getId();

        UserDto result = userService.getById(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals(newUser.getName(), result.getName());
        assertEquals(newUser.getEmail(), result.getEmail());

        Optional<User> dbUser = userRepository.findById(userId);
        assertTrue(dbUser.isPresent());
        assertEquals(newUser.getName(), dbUser.get().getName());
        assertEquals(newUser.getEmail(), dbUser.get().getEmail());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUserDoesNotExist_Integration() {
        Long nonExistentId = 999L;

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.getById(nonExistentId)
        );

        assertTrue(exception.getMessage().contains("Пользователь не найден по ID " + nonExistentId));

        Optional<User> nonExistentUser = userRepository.findById(nonExistentId);
        assertFalse(nonExistentUser.isPresent());
    }

    @Test
    void shouldUpdateUserSuccessfully_Integration() {
        NewUser newUser = createNewUser();
        UserDto createdUser = userService.create(newUser);
        Long userId = createdUser.getId();

        UpdateUser updateUser = UpdateUser.builder()
                .name("Updated Name")
                .email("updated@example.com")
                .build();

        UserDto updatedUser = userService.update(updateUser, userId);

        assertNotNull(updatedUser);
        assertEquals(userId, updatedUser.getId());
        assertEquals("Updated Name", updatedUser.getName());
        assertEquals("updated@example.com", updatedUser.getEmail());

        Optional<User> dbUser = userRepository.findById(userId);
        assertTrue(dbUser.isPresent());
        assertEquals("Updated Name", dbUser.get().getName());
        assertEquals("updated@example.com", dbUser.get().getEmail());
    }

    @Test
    void shouldUpdateOnlyNameSuccessfully_Integration() {
        NewUser newUser = createNewUser();
        UserDto createdUser = userService.create(newUser);
        Long userId = createdUser.getId();

        UpdateUser updateUser = UpdateUser.builder()
                .name("New Name")
                .build();

        UserDto updatedUser = userService.update(updateUser, userId);

        assertNotNull(updatedUser);
        assertEquals(userId, updatedUser.getId());
        assertEquals("New Name", updatedUser.getName());
        assertEquals(newUser.getEmail(), updatedUser.getEmail());

        Optional<User> dbUser = userRepository.findById(userId);
        assertTrue(dbUser.isPresent());
        assertEquals("New Name", dbUser.get().getName());
        assertEquals(newUser.getEmail(), dbUser.get().getEmail());
    }

    @Test
    void shouldUpdateOnlyEmailSuccessfully_Integration() {
        NewUser newUser = createNewUser();
        UserDto createdUser = userService.create(newUser);
        Long userId = createdUser.getId();

        UpdateUser updateUser = UpdateUser.builder()
                .email("newemail@example.com")
                .build();

        UserDto updatedUser = userService.update(updateUser, userId);

        assertNotNull(updatedUser);
        assertEquals(userId, updatedUser.getId());
        assertEquals(newUser.getName(), updatedUser.getName()); // name остался прежним
        assertEquals("newemail@example.com", updatedUser.getEmail());

        Optional<User> dbUser = userRepository.findById(userId);
        assertTrue(dbUser.isPresent());
        assertEquals(newUser.getName(), dbUser.get().getName());
        assertEquals("newemail@example.com", dbUser.get().getEmail());
    }

    @Test
    void shouldThrowValidationExceptionWhenUpdateWithEmailExists_Integration() {
        NewUser user1 = createNewUser();
        NewUser user2 = NewUser.builder()
                .name("Bob")
                .email("bob@example.com")
                .build();

        UserDto user1Dto = userService.create(user1);
        userService.create(user2);

        UpdateUser updateUser = UpdateUser.builder()
                .email(user2.getEmail())
                .build();

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userService.update(updateUser, user1Dto.getId())
        );

        assertTrue(exception.getMessage().contains("Данный email уже есть в базе " + user2.getEmail()));

        Optional<User> dbUser1 = userRepository.findById(user1Dto.getId());
        assertTrue(dbUser1.isPresent());
        assertEquals("alice@example.com", dbUser1.get().getEmail());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUpdateUserDoesNotExist_Integration() {
        Long nonExistentId = 999L;
        UpdateUser updateUser = UpdateUser.builder()
                .name("Non Existent")
                .email("nonexistent@example.com")
                .build();

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.update(updateUser, nonExistentId)
        );

        assertTrue(exception.getMessage().contains("Пользователь не найден по ID " + nonExistentId));

        Optional<User> nonExistentUser = userRepository.findById(nonExistentId);
        assertFalse(nonExistentUser.isPresent());
    }

    @Test
    void shouldDeleteUserSuccessfully_Integration() {
        NewUser newUser = createNewUser();
        UserDto createdUser = userService.create(newUser);
        Long userId = createdUser.getId();

        Optional<User> userBeforeDelete = userRepository.findById(userId);
        assertTrue(userBeforeDelete.isPresent());

        userService.delete(userId);

        Optional<User> userAfterDelete = userRepository.findById(userId);

        assertFalse(userAfterDelete.isPresent(), "Пользователь должен быть удалён из БД");
    }

    @Test
    void shouldNotThrowExceptionWhenDeletingNonExistentUser_Integration() {
        Long nonExistentId = 999L;
        NewUser anotherUser = createNewUser();
        UserDto anotherCreatedUser = userService.create(anotherUser);

        Optional<User> nonExistentUser = userRepository.findById(nonExistentId);
        assertFalse(nonExistentUser.isPresent());

        assertDoesNotThrow(
                () -> userService.delete(nonExistentId),
                "Метод delete не должен выбрасывать исключение при удалении несуществующего пользователя"
        );

        Optional<User> checkAnotherUser = userRepository.findById(anotherCreatedUser.getId());
        assertTrue(checkAnotherUser.isPresent(), "Другие пользователи не должны быть затронуты");
    }

    private NewUser createNewUser() {
        return NewUser.builder()
                .name("Alice")
                .email("alice@example.com")
                .build();
    }
}
