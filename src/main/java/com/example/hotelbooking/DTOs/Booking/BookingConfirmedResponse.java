package com.example.hotelbooking.DTOs.Booking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingConfirmedResponse {

    private String bookingId;
    private String paymentId;
    private LocalDate checkin;
    private LocalDate checkOut;

}
