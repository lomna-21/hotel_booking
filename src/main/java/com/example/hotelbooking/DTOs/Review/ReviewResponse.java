package com.example.hotelbooking.DTOs.Review;

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
public class ReviewResponse {
    private String publicId;
    private String hotelPublicId;
    private String hotelName;
    private BigDecimal rating;
    private String comment;
    private LocalDateTime createdAt;
}
