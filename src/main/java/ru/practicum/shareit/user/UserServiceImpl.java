package ru.practicum.shareit.user;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.user.model.NewUser;
import ru.practicum.shareit.user.model.UpdateUser;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.UserDto;

@Slf4j
@Service
@RequiredArgsConstructor
@Validated
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserDto create(NewUser newUser) {
        log.info("Попытка создания нового пользователя {}", newUser);
        if (userRepository.emailIsNotAvailable(newUser.getEmail())) {
            log.error("Пользователь с email {} уже есть в базе", newUser.getEmail());
            throw new ValidationException("Данный email уже занят " + newUser.getEmail());
        }
        User user = UserMapper.toUser(newUser);
        log.info("Пользователь после маппинга {}", user);
        user = userRepository.create(user);
        log.info("Пользователь успешно создан в БД {}", user);
        return UserMapper.toDto(user);
    }

    public UserDto getById(Long id) {
        log.info("Попытка получения пользователя по ID {}", id);
        User user = userRepository.getById(id);
        log.info("Получен пользователь из БД {}", user);
        return UserMapper.toDto(user);
    }

    public UserDto update(UpdateUser updateUser, Long userId) {
        log.info("Попытка обновления данных {} пользователя с ID {}", updateUser, userId);
        if (updateUser.hasEmail() && userRepository.emailIsNotAvailable(updateUser.getEmail())) {
            log.error("Email {} недоступен", updateUser.getEmail());
            throw new ValidationException("Данный email уже есть в базе " + updateUser.getEmail());
        }
        User user = userRepository.getById(userId);
        log.info("Из базы по ID {} получен пользователь {}", userId, user);
        user = UserMapper.toUser(user, updateUser);
        log.info("После маппинга с обновленным пользователем {}", user);
        user = userRepository.update(user);
        log.info("Обновленный юзер в БД {}", user);
        return UserMapper.toDto(user);
    }

    public void delete(Long id) {
        log.info("Попытка удаления пользователя по ID {}", id);
        userRepository.delete(id);
        log.info("Пользователь удален по ID {}", id);
    }
}