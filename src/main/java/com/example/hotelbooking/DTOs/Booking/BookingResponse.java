package com.example.hotelbooking.DTOs.Booking;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingResponse {
    private String publicId;
    private String hotelPublicId;
    private String roomPublicId;
    private String roomType;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private BigDecimal totalAmount;
    private String paymentPublicId;
    private String bookingStatus;
    private String bookingType;
    private LocalDateTime createdAt;
}
