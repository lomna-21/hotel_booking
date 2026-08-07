package com.example.EventDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingEventDto {
    private String bookingPublicId;
    private String hotelPublicId;
    private String guestEmail;
    private LocalDate checkIn;
    private LocalDate checkOut;
}
