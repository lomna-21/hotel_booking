package com.example.hotelbooking.Services.Payment;

import com.example.hotelbooking.DTOs.Booking.BookingConfirmedResponse;
import com.example.hotelbooking.DTOs.Booking.BookingResponse;
import com.example.hotelbooking.DTOs.Payment.CreatePaymentRequest;
import com.example.hotelbooking.ExceptionHandler.UnauthorizedException;
import com.example.hotelbooking.Models.Booking;
import com.example.hotelbooking.Models.CustomUserDetails;
import com.example.hotelbooking.Respositories.BookingRepository;
import com.example.hotelbooking.Respositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {


    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public ResponseEntity<String> makeOnlinePayment(String paymentPublicId, CreatePaymentRequest request) {

        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();

        Booking booking = bookingRepository.findByPublicIdAndBookedBy_Id(
                request.getBookingPublicId(), userDetails.getUser().getId()).orElseThrow(
                () -> new UnauthorizedException("Booking not found or doesnt belong to the user")
        );
        try {
            paymentRepository.updatePaymentStatusByPublicId(paymentPublicId, "COMPLETED");

            bookingRepository.updateBookingStatusById(booking.getId(), "CONFIRMED");
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
