package ru.practicum.shareit.item;

import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.dto.NewComment;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemFullDto;
import ru.practicum.shareit.item.dto.NewItem;
import ru.practicum.shareit.item.dto.UpdateItem;

import java.util.List;

/**
 * Сервис для управления предметами (вещами) в системе аренды.
 * Предоставляет бизнес‑логику для операций с предметами, включая:
 * создание новых предметов;
 * обновление существующих записей;
 * получение информации по идентификатору;
 * просмотр всех предметов пользователя;
 * поиск доступных предметов по текстовому параметру.
 * Ключевые особенности:
 * все методы возвращают {@link ItemDto} или коллекции {@link ItemDto} —
 * объекты передачи данных, изолированные от внутренней модели;
 * операции требуют указания {@code userId} для проверки прав доступа;
 * поиск ({@link #searchAvailableItemByParam}) работает только по доступным предметам.
 */

public interface ItemService {

    /**
     * Создаёт новый предмет в системе.
     *
     * @param newItem DTO с данными нового предмета (название, описание, доступность).
     *                Должен соответствовать правилам валидации {@link NewItem}.
     * @param userId  Идентификатор пользователя‑владельца предмета.
     *                Используется для привязки предмета к пользователю и проверки прав.
     * @return {@link ItemDto} с данными созданного предмета, включая присвоенный ID.
     * @throws NotFoundException если {@code userId} отсутствует в базе
     */
    ItemDto create(NewItem newItem, Long userId);

    /**
     * Обновляет существующий предмет частично.
     *
     * @param updateItem DTO с новыми значениями полей.
     *                   Поля, равные null, игнорируются (не обновляются).
     * @param itemId     Идентификатор предмета, который требуется обновить.
     * @param userId     Идентификатор пользователя‑владельца.
     *                   Используется для проверки прав на изменение.
     * @return {@link ItemDto} с актуальными данными предмета после обновления.
     * @throws NotFoundException если {@code itemId} не существует,
     *                           либо {@code userId} не имеет прав на изменение
     */
    ItemDto update(UpdateItem updateItem, Long itemId, Long userId);

    /**
     * Получает информацию о предмете по его идентификатору.
     *
     * @param id Идентификатор предмета.
     * @return {@link ItemDto} с данными предмета.
     * @throws NotFoundException если предмет с указанным {@code id} не найден
     */
    ItemFullDto getById(Long id);

    /**
     * Возвращает список всех предметов, принадлежащих пользователю.
     *
     * @param userId Идентификатор пользователя‑владельца.
     * @return Список {@link ItemDto} всех предметов пользователя.
     * Может быть пустым, если у пользователя нет предметов.
     */
    List<ItemFullDto> getAllByUserId(Long userId);

    /**
     * Ищет доступные для аренды предметы по текстовому параметру.
     * Условия поиска:
     * ищутся только предметы со статусом {@code available = true};
     * поиск выполняется по полям {@code name} и {@code description} (частичное совпадение);
     * регистр не учитывается.
     *
     * @param text Текстовый параметр для поиска (например, "стул", "деревянный").
     *             Может быть пустым — в этом случае возвращается пустой список.
     * @return Список {@link ItemDto} подходящих предметов.
     */
    List<ItemDto> searchAvailableItemByParam(String text);

    /**
     * Создаёт новый комментарий к предмету от имени указанного пользователя.
     * Обрабатывает запрос на добавление отзыва о предмете: связывает комментарий
     * с конкретным предметом и его автором, сохраняет в системе и возвращает
     * заполненный DTO с данными нового комментария.
     * <p>Особенности:
     * <ul>
     *   <li>проверяет существование предмета и пользователя;</li>
     *   <li>автоматически устанавливает дату создания комментария;</li>
     *   <li>связывает комментарий с автором и предметом в БД;</li>
     * </ul>
     *
     * @param itemId  идентификатор предмета, к которому добавляется комментарий (не может быть {@code null})
     * @param userId  идентификатор пользователя‑автора комментария (не может быть {@code null})
     * @param comment DTO с текстом комментария ({@link NewComment}, не может быть {@code null})
     * @return {@link CommentDto} заполненный объект с данными нового комментария, включая сгенерированный ID и дату создания
     * @throws NotFoundException   если {@code itemId} или {@code userId} не существуют в системе
     * @throws ValidationException если текст комментария не соответствует правилам валидации
     */
    CommentDto createNewComment(Long itemId, Long userId, NewComment comment);
}