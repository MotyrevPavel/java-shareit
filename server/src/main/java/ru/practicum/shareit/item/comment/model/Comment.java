package ru.practicum.shareit.item.comment.model;

import jakarta.persistence.*;
import lombok.Data;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

/**
 * Сущность комментария к предмету в системе аренды.
 * Представляет запись о комментарии в базе данных, включая текст, ссылку на предмет,
 * автора и дату создания. Соответствует таблице {@code comments} в БД.
 *
 * <p>Основные связи:
 * <ul>
 *   <li>связан с сущностью {@link Item} (предмет, к которому оставлен комментарий);</li>
 *   <li>связан с сущностью {@link User} (пользователь, который оставил комментарий).</li>
 * </ul>
 *
 * <p>Особенности реализации:
 * <ul>
 *   <li>использует автоинкрементный идентификатор ({@link GenerationType#IDENTITY});</li>
 *   <li>поле {@code text} не может быть {@code null} — комментарий должен содержать текст;</li>
 *   <li>дата создания ({@code created}) устанавливается при сохранении и не редактируется;</li>
 *   <li>связи с {@link Item} и {@link User} загружаются лениво ({@link FetchType#LAZY}).</li>
 * </ul>
 *
 * @see Item сущность предмета, к которому относится комментарий
 * @see User сущность пользователя (автора комментария)
 */

@Data
@Entity
@Table(name = "comments")
public class Comment {

    /**
     * Уникальный идентификатор комментария в системе.
     *
     * <p>Автоматически генерируется БД при сохранении новой записи
     * (стратегия {@link GenerationType#IDENTITY}).
     *
     * <p>Особенности:
     * <ul>
     *   <li>первичный ключ таблицы {@code comments};</li>
     *   <li>не может быть {@code null} после сохранения в БД;</li>
     *   <li>используется для однозначной идентификации комментария.</li>
     * </ul>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Текст комментария, оставленный пользователем.
     *
     * <p>Содержит отзыв, оценку или дополнительную информацию о предмете.
     * Соответствует колонке {@code text} в таблице {@code comments}.
     *
     * <p>Особенности хранения в БД:
     * <ul>
     *   <li>не может быть {@code null};</li>
     *   <li>может содержать эмодзи, знаки препинания и переносы строк.</li>
     * </ul>
     */
    @Column(name = "text", nullable = false)
    private String text;

    /**
     * Предмет, к которому оставлен комментарий.
     *
     * <p>Ссылка на сущность {@link Item}, представляющую предмет в каталоге.
     * Связана с колонкой {@code item_id} в таблице {@code comments}.
     *
     * <p>Особенности связи:
     * <ul>
     *   <li>связь «многие‑к‑одному» ({@link ManyToOne});</li>
     *   <li>загрузка по требованию ({@link FetchType#LAZY});</li>
     *   <li>не может быть {@code null} — каждый комментарий относится к конкретному предмету.</li>
     * </ul>
     *
     * @see Item модель предмета в системе
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    /**
     * Автор комментария (пользователь системы).
     *
     * <p>Ссылка на сущность {@link User}, представляющую пользователя, который
     * оставил отзыв. Связана с колонкой {@code user_id} в таблице {@code comments}.
     *
     * <p>Особенности связи:
     * <ul>
     *   <li>связь «многие‑к‑одному» ({@link ManyToOne});</li>
     *   <li>загрузка по требованию ({@link FetchType#LAZY});</li>
     *   <li>не может быть {@code null} — каждый комментарий имеет автора.</li>
     * </ul>
     *
     * @see User модель пользователя в системе
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    /**
     * Дата и время создания комментария.
     *
     * <p>Указывает, когда комментарий был оставлен пользователем. Соответствует
     * колонке {@code created} в таблице {@code comments}.
     *
     * <p>Особенности:
     * <ul>
     *   <li>устанавливается автоматически на стороне сервера в момент сохранения;</li>
     *   <li>не может быть {@code null};</li>
     *   <li>не редактируется после создания;</li>
     *   <li>формат хранения соответствует {@link LocalDateTime}.</li>
     * </ul>
     */
    @Column(name = "created", nullable = false)
    private LocalDateTime created;
}
