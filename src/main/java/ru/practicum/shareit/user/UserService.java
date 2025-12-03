package ru.practicum.shareit.user;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.model.NewUser;
import ru.practicum.shareit.user.model.UpdateUser;
import ru.practicum.shareit.user.model.UserDto;

/**
 * Сервис для управления пользователями в приложении ShareIt.
 * Предоставляет базовый набор CRUD-операций для работы с пользователями,
 * используя DTO-объекты для передачи данных между слоями приложения.
 * Основные принципы реализации:
 * Изоляция внутренней модели данных от внешнего API
 * Контроль передаваемой информации через DTO
 * Валидация входных данных
 * Обработка ошибок через исключения
 */

public interface UserService {

    /**
     * Создаёт нового пользователя на основе переданных данных.
     * Выполняет:
     * валидацию входных данных
     * сохранение пользователя в хранилище
     * возвращение полной информации о созданном пользователе
     *
     * @param newUser объект с данными для регистрации пользователя
     *                (обязательна валидация через {@link jakarta.validation.Valid})
     * @return UserDto заполненный DTO созданного пользователя включая сгенерированный идентификатор
     * @throws ConstraintViolationException при нарушении валидационных ограничений
     * @throws ValidationException          при конфликте email
     */
    UserDto create(NewUser newUser);

    /**
     * Получает данные пользователя по его идентификатору.
     * Выполняет:
     * поиск пользователя по ID в хранилище
     * возвращение только публичных данных (без чувствительных полей)
     * не изменяет состояние объекта
     *
     * @param id уникальный идентификатор пользователя
     * @return UserDto DTO с информацией о пользователе
     * @throws NotFoundException если пользователь с указанным ID не существует
     */
    UserDto getById(Long id);

    /**
     * Обновляет данные существующего пользователя.
     * Выполняет:
     * проверку существования пользователя по ID
     * применение только указанных изменений (частичное обновление)
     * сохранение обновлённых данных в хранилище
     * возвращение полного актуального DTO пользователя
     *
     * @param updateUser объект с новыми данными пользователя
     *                   (обязательна валидация через {@link jakarta.validation.Valid})
     * @param userId     уникальный идентификатор пользователя
     * @return UserDto DTO с актуальными данными пользователя после обновления
     * @throws ConstraintViolationException при нарушении валидационных ограничений
     * @throws ValidationException          при конфликте email (если пользователю присваивается
     *                                      новый email который уже есть в базе)
     * @throws NotFoundException            если пользователь с указанным ID не существует
     */
    UserDto update(UpdateUser updateUser, Long userId);

    /**
     * Удаляет пользователя по идентификатору.
     * Выполняет:
     * поиск пользователя по ID
     * удаление записи из хранилища
     * не возвращает данных после выполнения
     *
     * @param id идентификатор удаляемого пользователя
     * @throws NotFoundException если пользователь с указанным ID не существует
     */
    void delete(Long id);
}