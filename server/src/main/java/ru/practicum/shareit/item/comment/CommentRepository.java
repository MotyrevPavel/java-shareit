package ru.practicum.shareit.item.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.comment.model.Comment;

import java.util.List;
import java.util.Set;

/**
 * Репозиторий для работы с сущностями {@link Comment} (комментарии к предметам) в системе аренды.
 * Предоставляет методы для поиска комментариев по идентификаторам предметов. Наследует
 * функционал {@link JpaRepository} для базовых операций CRUD.
 *
 * <p>Ключевые возможности:
 * <ul>
 *   <li>поиск всех комментариев для конкретного предмета;</li>
 *   <li>поиск комментариев для набора предметов (по списку ID).</li>
 * </ul>
 *
 * <p>Особенности реализации:
 * <ul>
 *   <li>использует JPQL‑запросы для гибкой выборки данных;</li>
 *   <li>возвращает полные сущности {@link Comment}, включая связи с {@link ru.practicum.shareit.item.model.Item}
 *       и {@link ru.practicum.shareit.user.model.User};</li>
 *   <li>оптимизирован для сценариев, где требуется получить комментарии
 *       для отображения вместе с информацией о предметах.</li>
 * </ul>
 *
 * @see Comment сущность комментария в системе
 * @see JpaRepository базовый репозиторий Spring Data JPA
 */

public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * Находит все комментарии, оставленные для указанного предмета.
     *
     * <p>Выполняет поиск всех комментариев, связанных с предметом,
     * идентифицированным по {@code itemId}. Возвращает комментарии
     * в порядке их создания (от старых к новым).
     *
     * <p>JPQL‑запрос:
     * <pre>
     * SELECT c FROM Comment c
     * WHERE c.item.id = :itemId
     * </pre>
     *
     * <p>Используется для:
     * <ul>
     *   <li>отображения списка комментариев на странице предмета;</li>
     *   <li>анализа обратной связи по конкретному предмету.</li>
     * </ul>
     *
     * @param itemId идентификатор предмета, для которого ищутся комментарии
     *               (не может быть {@code null}, должен соответствовать
     *               существующему предмету в системе)
     * @return список сущностей {@link Comment}, связанных с указанным предметом
     *         (пустой список, если комментариев не найдено)
     */
    @Query("SELECT c FROM Comment c " +
            "WHERE c.item.id = :itemId")
    List<Comment> findCommentsByItemId(@Param("itemId") Long itemId);

    /**
     * Находит все комментарии, оставленные для набора предметов.
     *
     * <p>Выполняет поиск комментариев, связанных с любым из предметов,
     * чьи идентификаторы переданы в наборе {@code itemIds}. Позволяет
     * эффективно загружать комментарии для нескольких предметов за один запрос.
     *
     * <p>JPQL‑запрос:
     * <pre>
     * SELECT c FROM Comment c
     * WHERE c.item.id IN :itemIds
     * </pre>
     *
     * <p>Используется для:
     * <ul>
     *   <li>отображения комментариев в списке доступных предметов;</li>
     *   <li>формирования дайджеста отзывов за период;</li>
     *   <li>массовой обработки комментариев (например, при удалении предметов).</li>
     * </ul>
     *
     * <p>Пример использования: получить все комментарии для предметов,
     * арендованных пользователем за последний месяц.
     *
     * @param itemIds набор идентификаторов предметов, для которых ищутся комментарии
     *              (не может быть {@code null}, может быть пустым — в этом случае
     *              возвращается пустой список)
     * @return список сущностей {@link Comment}, связанных с указанными предметами
     *         (пустой список, если ни для одного предмета нет комментариев
     *         или если {@code itemIds} пуст)
     */
    @Query("SELECT c FROM Comment c " +
            "WHERE c.item.id IN :itemIds")
    List<Comment> findCommentsByItemIds(@Param("itemIds") Set<Long> itemIds);

}