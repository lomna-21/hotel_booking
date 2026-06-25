package com.example.hotelbooking.Respositories;

import com.example.hotelbooking.Models.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByPublicId(String publicId);
    List<Booking> findAllByCustomerId(Long customerId);
    List<Booking> findAllByBookedBy_Id(Long userId);
    List<Booking> findAllByHotelId(Long hotelId);

    @Query("SELECT b FROM Booking b WHERE b.bookingStatus = 'CONFIRMED' " +
            "AND b.checkIn = :today")
    List<Booking> findConfirmedCheckingInToday(@Param("today") LocalDate today);

    @Query("SELECT b FROM Booking b WHERE b.bookingStatus = 'CONFIRMED' " +
            "AND b.checkOut = :today")
    List<Booking> findConfirmedCheckingOutToday(@Param("today") LocalDate today);

    @Query("SELECT b FROM Booking b WHERE b.bookingStatus = 'PENDING' " +
            "AND b.createdAt < :expiryTime")
    List<Booking> findExpiredPendingBookings(@Param("expiryTime") LocalDateTime expiryTime);

    Optional<Booking> findByPublicIdAndBookedBy_Id(String publicId, Long bookedById);

    @Modifying
    @Query("UPDATE Booking b SET b.bookingStatus = :status WHERE b.id = :id")
    void updateBookingStatusById(
            @Param("id") Long id,
            @Param("status") String status
    );

    Optional<Booking> findByPublicIdAndCustomer_Id(String publicId, Long customerId);

    @Query("SELECT b.room.id FROM Booking b WHERE b.bookingStatus = 'CONFIRMED' AND b.checkIn <= CURRENT_DATE AND b.checkOut >= CURRENT_DATE")
    Set<Long> findCurrentlyBookedRoomIds();

    @Query("SELECT b.room.id FROM Booking b WHERE b.bookingStatus = 'CONFIRMED' AND b.checkIn < :checkOut AND b.checkOut > :checkIn")
    Set<Long> findBookedRoomIdsBetween(@Param("checkIn") LocalDate checkIn, @Param("checkOut") LocalDate checkOut);

}