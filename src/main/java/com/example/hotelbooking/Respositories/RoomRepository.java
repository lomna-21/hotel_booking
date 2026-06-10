package com.example.hotelbooking.Respositories;

import com.example.hotelbooking.Models.Room;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByPublicId(String publicId);
    boolean existsByHotel_IdAndRoomNumber(Long hotelId, String roomNumber);
    boolean existsByHotel_IdAndRoomType(Long hotelId, String roomType);
    @Query("SELECT COUNT(r) FROM Room r WHERE r.hotel.id = :hotelId " +
            "AND r.roomType = :roomType " +
            "AND r.roomStatus = 'AVAILABLE'")
    long countAvailableRooms(
            @Param("hotelId") Long hotelId,
            @Param("roomType") String roomType
    );
    long countByHotel_IdAndRoomType(Long hotelId, String roomType);

    @Query("SELECT r FROM Room r WHERE r.hotel.id = :hotelId " +
            "AND r.roomType = :roomType " +
            "AND r.roomStatus = 'AVAILABLE' " +
            "ORDER BY r.id ASC")
    List<Room> findAvailableRooms(
            @Param("hotelId") Long hotelId,
            @Param("roomType") String roomType,
            Pageable pageable
    );

    @Query(value = "SELECT * FROM rooms WHERE hotel_id = :hotelId " +
            "AND room_type = :roomType " +
            "AND room_status = 'AVAILABLE' " +
            "LIMIT 1 FOR UPDATE SKIP LOCKED",
            nativeQuery = true)
    Optional<Room> findOneAvailableWithSkipLocked(
            @Param("hotelId") Long hotelId,
            @Param("roomType") String roomType
    );

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.room.hotel.id = :hotelId " +
            "AND b.room.roomType = :roomType " +
            "AND b.bookingStatus != 'CANCELLED' " +
            "AND b.checkIn < :checkOut " +
            "AND b.checkOut > :checkIn")
    long countOverlappingBookings(
            @Param("hotelId") Long hotelId,
            @Param("roomType") String roomType,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );

    List<Room> findAllByHotel_Id(Long hotelId);

    @Query("SELECT r FROM Room r WHERE r.hotel.id = :hotelId " +
            "AND r.roomType = :roomType " +
            "AND r.roomStatus = 'AVAILABLE' " +
            "ORDER BY r.id ASC")
    Optional<Room> findOneAvailable(
            @Param("hotelId") Long hotelId,
            @Param("roomType") String roomType
    );
}
