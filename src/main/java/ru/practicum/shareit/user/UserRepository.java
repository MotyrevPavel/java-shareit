package ru.practicum.shareit.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import ru.practicum.shareit.user.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Проверяет, существует ли пользователь с указанным email в БД.
     *
     * @param email email пользователя
     * @return true, если пользователь с таким email есть; false — если нет
     */
    boolean existsByEmail(String email);

    /**
     * Проверяет, существует ли пользователь с указанным идентификатором в БД.
     *
     * @param id уникальный идентификатор пользователя (первичный ключ)
     * @return true, если пользователь с таким id найден в базе данных;
     * false — если пользователь не существует
     */
    boolean existsById(@NonNull Long id);
}
