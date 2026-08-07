package com.example.hotelbooking.Services.Payment;

import com.example.EventDtos.BookingEventDto;
import com.example.hotelbooking.DTOs.Payment.CreatePaymentRequest;
import com.example.hotelbooking.ExceptionHandler.UnauthorizedException;
import com.example.hotelbooking.Models.Booking;
import com.example.hotelbooking.Models.CustomUserDetails;
import com.example.hotelbooking.Respositories.BookingRepository;
import com.example.hotelbooking.Respositories.HotelRepository;
import com.example.hotelbooking.Respositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final HotelRepository hotelRepository;
    private final KafkaTemplate<String, BookingEventDto> kafkaTemplate;

    @Transactional
    public ResponseEntity<String> makeOnlinePayment(String paymentPublicId, CreatePaymentRequest request) {

        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();

        Booking booking = bookingRepository.findByPublicIdAndBookedBy_Id(
                request.getBookingPublicId(), userDetails.getUser().getId()).orElseThrow(
                () -> new UnauthorizedException("Booking not found or doesnt belong to the user")
        );


        BookingEventDto bookingEventDto = BookingEventDto.builder().bookingPublicId(booking.getPublicId())
                .hotelPublicId(booking.getHotel().getPublicId()).guestEmail(userDetails.getUser().getEmail())
                .checkIn(booking.getCheckIn()).checkOut(booking.getCheckOut()).build();
        try {
            paymentRepository.updatePaymentStatusByPublicId(paymentPublicId, "COMPLETED");

            bookingRepository.updateBookingStatusById(booking.getId(), "CONFIRMED");
            kafkaTemplate.send("booking-topic", booking.getPublicId(), bookingEventDto);
            String message =
                    "Booking has been confirmed with Payment Id: "+paymentPublicId+"\n"+
                    "and"+"\n"+
                    "Booking Id: "+request.getBookingPublicId()+"."+"\n"+
                    "Check In : "+booking.getCheckIn().toString()+"\n"+
                    "Check Out :"+booking.getCheckOut().toString();
            return ResponseEntity.ok().body(message);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
