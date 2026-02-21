package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Репозиторий для работы с сущностями {@link Booking} в системе аренды.
 * Предоставляет методы для поиска бронирований по различным критериям:
 * пользователю, владельцу предмета, статусу и идентификатору предмета.
 * Наследует функционал {@link JpaRepository} для базовых операций CRUD.
 * Ключевые возможности:
 * поиск бронирований арендатора с фильтрацией по статусу;
 * поиск бронирований владельца предмета с фильтрацией по статусу;
 * поиск конкретных бронирований по комбинации предмет‑арендатор‑статус;
 * получение всех бронирований для конкретного предмета.
 *
 * @see Booking сущность бронирования в системе
 * @see JpaRepository базовый репозиторий Spring Data JPA
 */

public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Находит бронирования пользователя с фильтрацией по статусу.
     * Выполняет поиск всех бронирований, принадлежащих указанному арендатору.
     * Поддерживает специальный статус {@code 'ALL'}, который возвращает все бронирования
     * пользователя независимо от их статуса.
     * JPQL‑запрос:
     * SELECT b FROM Booking b
     * WHERE b.booker.id = :bookerId
     * AND (:status = 'ALL' OR b.status = :status)
     * Параметры:
     * {@code bookerId} — идентификатор пользователя‑арендатора;
     * {@code status} — статус бронирований для фильтрации или {@code 'ALL'}
     * для получения всех статусов.
     * Примеры использования:
     * получить все бронирования пользователя: {@code status = 'ALL'};
     * получить только подтверждённые бронирования: {@code status = BookingState.APPROVED}.
     *
     * @param bookerId идентификатор пользователя, создавшего бронирования (не может быть {@code null})
     * @param status   статус бронирований для фильтрации ({@link BookingState}) или
     *                 строка {@code 'ALL'} для получения всех статусов
     * @return список бронирований пользователя, соответствующих критериям поиска
     * (пустой список, если бронирований не найдено)
     */
    @Query("SELECT b FROM Booking b " +
            "WHERE b.booker.id = :bookerId " +
            "AND (:status = 'ALL' OR b.status = :status)")
    List<Booking> findByBookerIdAndStatus(@Param("bookerId") Long bookerId,
                                          @Param("status") BookingState status);

    /**
     * Находит бронирования для предметов, принадлежащих владельцу, с фильтрацией по статусу.
     * Ищет все бронирования, относящиеся к предметам, которыми владеет указанный пользователь.
     * Аналогично предыдущему методу поддерживает статус {@code 'ALL'} для получения
     * всех бронирований без фильтрации.
     * JPQL‑запрос:
     * SELECT b FROM Booking b
     * WHERE b.item.user.id = :ownerId
     * AND (:status = 'ALL' OR b.status = :status)
     * Пример: владелец может увидеть все ожидающие подтверждения бронирования
     * для своих предметов, установив {@code status = BookingState.WAITING}.
     *
     * @param ownerId идентификатор владельца предметов (не может быть {@code null})
     * @param status  статус бронирований для фильтрации ({@link BookingState}) или
     *                строка {@code 'ALL'} для получения всех статусов
     * @return список бронирований для предметов владельца, соответствующих критериям
     * (пустой список, если бронирований не найдено)
     */
    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.user.id = :ownerId " +
            "AND (:status = 'ALL' OR b.status = :status)")
    List<Booking> findByItemOwnerIdAndStatus(@Param("ownerId") Long ownerId,
                                             @Param("status") BookingState status);

    /**
     * Находит конкретное бронирование по комбинации предмет‑арендатор‑статус.
     * JPQL‑запрос:
     * SELECT b FROM Booking b
     * WHERE b.item.id = :itemId
     * AND b.booker.id = :bookerId
     * AND b.status = :status
     *
     * @param itemId   идентификатор предмета, который бронируется (не может быть {@code null})
     * @param bookerId идентификатор пользователя, создавшего бронирование (не может быть {@code null})
     * @param status   статус бронирования для поиска ({@link BookingState})
     * @return список бронирований, соответствующих всем трём критериям
     * (возвращает пустой список, если не найдено)
     */
    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.booker.id = :bookerId  " +
            "AND b.status = :status")
    List<Booking> findByItemIdAndBookerIdAndStatus(@Param("itemId") Long itemId,
                                                   @Param("bookerId") Long bookerId,
                                                   @Param("status") BookingState status);

    /**
     * Находит все бронирования для указанного предмета.
     * Возвращает полный список бронирований (всех статусов) для конкретного предмета.
     * Используется для:
     * отображения календаря бронирований предмета;
     * проверки доступности предмета на определённые даты;
     * анализа истории бронирований предмета.
     * JPQL‑запрос:
     * SELECT b FROM Booking b
     * WHERE b.item.id = :itemId
     *
     * @param itemId идентификатор предмета, для которого ищутся бронирования (не может быть {@code null})
     * @return список всех бронирований указанного предмета (пустой список, если бронирований нет)
     */
    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id = :itemId")
    List<Booking> findByItemId(@Param("itemId") Long itemId);
}