package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    /**
     * Возвращает список предметов (вещей), принадлежащих пользователю с указанным идентификатором.
     *
     * @param userId уникальный идентификатор пользователя‑владельца предметов (соответствует полю user_id в таблице items)
     * @return список объектов {@link Item}, принадлежащих пользователю с данным ID;
     * если предметов нет — возвращается пустой список (не null)
     * @implNote Метод использует связь Item и User для фильтрации по полю user_id.
     * SQL‑запрос будет иметь вид: SELECT * FROM items WHERE user_id = ?
     */
    List<Item> findByUserId(Long userId);

    /**
     * Ищет предметы, у которых:
     * - в названии (name) ИЛИ описании (description) содержится указанная подстрока (частичное совпадение);
     * - статус доступности available = true.
     * Поиск выполняется без учёта регистра.
     *
     * @param text поисковая подстрока (например, "дрель", "ремонт").
     *             Если null или пустая строка — возвращаются все доступные предметы.
     * @return список предметов, удовлетворяющих условиям;
     * если совпадений нет — возвращается пустой список (не null)
     * @implNote Использует JPQL с LOWER() и LIKE для игнорирования регистра и частичного совпадения.
     * Фильтрация по available = true выполняется явно в запросе.
     */
    @Query("SELECT i FROM Item i " +
            "WHERE (LOWER(i.name) LIKE CONCAT('%', LOWER(:text), '%') " +
            "   OR LOWER(i.description) LIKE CONCAT('%', LOWER(:text), '%')) " +
            "   AND i.available = true")
    List<Item> searchAvailableItemByParam(@Param("text") String text);
}