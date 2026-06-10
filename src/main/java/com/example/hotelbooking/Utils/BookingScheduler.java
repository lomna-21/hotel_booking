package com.example.hotelbooking.Utils;

import com.example.hotelbooking.Models.Booking;
import com.example.hotelbooking.Respositories.BookingRepository;
import com.example.hotelbooking.Respositories.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingScheduler {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;

    // runs every minute — cancels expired PENDING bookings
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cancelExpiredPendingBookings() {
        LocalDateTime expiryTime = LocalDateTime.now().minusMinutes(10);
        List<Booking> expiredBookings = bookingRepository.findExpiredPendingBookings(expiryTime);

        for (Booking booking : expiredBookings) {
            booking.setBookingStatus("CANCELLED");
            booking.getRoom().setRoomStatus("AVAILABLE");
            roomRepository.save(booking.getRoom());
            bookingRepository.save(booking);
            log.info("Cancelled expired booking: {}", booking.getPublicId());
        }
    }

    // runs every hour — marks rooms as OCCUPIED on checkIn date
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void markRoomsOccupied() {
        LocalDate today = LocalDate.now();
        List<Booking> checkingIn = bookingRepository.findConfirmedCheckingInToday(today);

        for (Booking booking : checkingIn) {
            booking.getRoom().setRoomStatus("OCCUPIED");
            roomRepository.save(booking.getRoom());
            log.info("Marked room as OCCUPIED for booking: {}", booking.getPublicId());
        }
    }

    // runs every hour — marks rooms as AVAILABLE on checkOut date
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void markRoomsAvailable() {
        LocalDate today = LocalDate.now();
        List<Booking> checkingOut = bookingRepository.findConfirmedCheckingOutToday(today);

        for (Booking booking : checkingOut) {
            booking.setBookingStatus("COMPLETED");
            booking.getRoom().setRoomStatus("AVAILABLE");
            roomRepository.save(booking.getRoom());
            bookingRepository.save(booking);
            log.info("Completed booking: {}", booking.getPublicId());
        }
    }
}
