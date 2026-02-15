package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT b FROM Booking b " +
            "WHERE b.booker.id = :bookerId " +
            "AND (:status = 'ALL' OR b.status = :status)")
    List<Booking> findByBookerIdAndStatus(@Param("bookerId") Long bookerId,
                                          @Param("status") BookingState status);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.user.id = :ownerId " +
            "AND (:status = 'ALL' OR b.status = :status)")
    List<Booking> findByItemOwnerIdAndStatus(@Param("ownerId") Long ownerId,
                                             @Param("status") BookingState status);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.booker.id = :bookerId  " +
            "AND b.status = :status")
    List<Booking> findByItemIdAndBookerIdAndStatus(@Param("itemId") Long itemId,
                                                   @Param("bookerId") Long bookerId,
                                                   @Param("status") BookingState status);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id = :itemId")
    List<Booking> findByItemId(@Param("itemId") Long itemId);
}