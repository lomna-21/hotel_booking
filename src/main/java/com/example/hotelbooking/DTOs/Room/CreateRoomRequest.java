package com.example.hotelbooking.DTOs.Room;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateRoomRequest {

    @NotBlank
    private String roomType;

    @NotBlank
    private String roomNumber;

    @NotNull
    private BigDecimal pricePerNight;

    @NotNull
    private Integer maxOccupancy;

    private String description;

    @NotBlank
    private String roomStatus;
//
//    @NotBlank
//    private String hotel_id;
}
