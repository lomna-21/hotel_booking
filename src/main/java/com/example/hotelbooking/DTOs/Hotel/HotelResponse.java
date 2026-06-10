package com.example.hotelbooking.DTOs.Hotel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelResponse {

    private String publicId;

    private String name;

    private String address;

    private String phone;

    private String email;

    private String website;

    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


}
