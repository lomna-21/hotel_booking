package com.example.hotelbooking.DTOs.Room;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomResponse {

    private String publicId;
    private String hotelPublicId;
    private String roomNumber;
    private String roomType;
    private String roomStatus;
    private Integer maxOccupancy;
    private String description;
    private BigDecimal pricePerNight;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
