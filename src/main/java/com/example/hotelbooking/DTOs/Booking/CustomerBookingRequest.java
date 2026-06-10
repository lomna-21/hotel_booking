package com.example.hotelbooking.DTOs.Booking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerBookingRequest {

    @NotBlank
    private String hotelPublicId;
    @NotBlank
    private String roomType;

    private LocalDate checkIn;

    private LocalDate checkOut;
}
