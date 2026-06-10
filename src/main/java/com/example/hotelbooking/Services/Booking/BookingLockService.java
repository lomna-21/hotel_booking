package com.example.hotelbooking.Services.Booking;

import com.example.hotelbooking.DTOs.Booking.BookingResponse;
import com.example.hotelbooking.DTOs.Booking.CustomerBookingRequest;
import com.example.hotelbooking.ExceptionHandler.NoRoomsAvailableException;
import com.example.hotelbooking.ExceptionHandler.RoomNotAvailableException;
import com.example.hotelbooking.Models.*;
import com.example.hotelbooking.Respositories.BookingRepository;
import com.example.hotelbooking.Respositories.PaymentRepository;
import com.example.hotelbooking.Respositories.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class BookingLockService {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;

    @Retryable(
            value = OptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    @Transactional
    public ResponseEntity<BookingResponse> optimisticLockBooking(
            User user, CustomerProfile customer,
            Hotel hotel, CustomerBookingRequest request,
            String bookingType) {
        try {
            // fetch room without any lock
            Room room = roomRepository.findOneAvailable(hotel.getId(), request.getRoomType())
                    .orElseThrow(() -> new NoRoomsAvailableException("No rooms available"));

            // calculate total amount
            long numberOfNights = ChronoUnit.DAYS.between(request.getCheckIn(), request.getCheckOut());
            BigDecimal totalAmount = room.getPricePerNight().multiply(BigDecimal.valueOf(numberOfNights));

            // set room status to BOOKED
            // @Version check happens automatically here
            // if another thread saved first, version mismatch → exception
            room.setRoomStatus("BOOKED");
            roomRepository.save(room);

            // create PENDING booking
            Booking booking = Booking.builder()
                    .hotel(hotel)
                    .room(room)
                    .customer(customer)
                    .bookedBy(user)
                    .firstName(customer.getFirstName())
                    .lastName(customer.getLastName())
                    .phone(customer.getPhone())
                    .email(customer.getUser() != null ? customer.getUser().getEmail() : null)
                    .checkIn(request.getCheckIn())
                    .checkOut(request.getCheckOut())
                    .totalAmount(totalAmount)
                    .bookingStatus("PENDING")
                    .bookingType(bookingType)
                    .build();

            Booking saved = bookingRepository.save(booking);
            Payment payment = Payment.builder()
                    .booking(saved)
                    .amount(totalAmount)
                    .status("PENDING")
                    .build();
            paymentRepository.save(payment);
            return ResponseEntity.ok(toBookingResponse(saved, payment));

        } catch (OptimisticLockingFailureException e) {
            throw new NoRoomsAvailableException("Room not available, please try again");
        }
    }

    @Recover
    public ResponseEntity<BookingResponse> optimisticLockRecover(
            OptimisticLockingFailureException e,
            User user, CustomerProfile customer,
            Hotel hotel, CustomerBookingRequest request) {
        throw new RoomNotAvailableException("Room was just booked by someone else, please try again");
    }

    @Transactional
    public ResponseEntity<BookingResponse> pessimisticLockBooking(
            User user, CustomerProfile customer,
            Hotel hotel, CustomerBookingRequest request,
            String bookingType) {

        // FOR UPDATE SKIP LOCKED — each thread gets a different row
        Room room = roomRepository.findOneAvailableWithSkipLocked(
                        hotel.getId(), request.getRoomType())
                .orElseThrow(() -> new NoRoomsAvailableException("No rooms available"));

        // calculate total amount
        long numberOfNights = ChronoUnit.DAYS.between(request.getCheckIn(), request.getCheckOut());
        BigDecimal totalAmount = room.getPricePerNight().multiply(BigDecimal.valueOf(numberOfNights));

        // set room status to BOOKED
        // lock is held until transaction commits
        room.setRoomStatus("BOOKED");
        roomRepository.save(room);

        // create PENDING booking
        Booking booking = Booking.builder()
                .hotel(hotel)
                .room(room)
                .customer(customer)
                .bookedBy(user)
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .phone(customer.getPhone())
                .email(customer.getUser() != null ? customer.getUser().getEmail() : null)
                .checkIn(request.getCheckIn())
                .checkOut(request.getCheckOut())
                .totalAmount(totalAmount)
                .bookingStatus("PENDING")
                .bookingType(bookingType)
                .build();

        Booking saved = bookingRepository.save(booking);
        Payment payment = Payment.builder()
                .booking(saved)
                .amount(totalAmount)
                .status("PENDING")
                .build();
        paymentRepository.save(payment);
        return ResponseEntity.ok(toBookingResponse(saved, payment));
    }

    private BookingResponse toBookingResponse(Booking booking, Payment payment) {
        return BookingResponse.builder()
                .publicId(booking.getPublicId())
                .hotelPublicId(booking.getHotel().getPublicId())
                .roomPublicId(booking.getRoom().getPublicId())
                .roomType(booking.getRoom().getRoomType())
                .firstName(booking.getFirstName())
                .lastName(booking.getLastName())
                .phone(booking.getPhone())
                .email(booking.getEmail())
                .checkIn(booking.getCheckIn())
                .checkOut(booking.getCheckOut())
                .totalAmount(booking.getTotalAmount())
                .paymentPublicId(payment.getPublicId())
                .bookingStatus(booking.getBookingStatus())
                .bookingType(booking.getBookingType())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}
