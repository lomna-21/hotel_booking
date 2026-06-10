package com.example.hotelbooking.DTOs.Room;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerRoomResponse {

    private String publicId;
    private String hotelPublicId;
    private String roomType;
    private String roomStatus;
    private Integer maxOccupancy;
    private String description;
    private BigDecimal pricePerNight;
    private Long availableRooms;
}
