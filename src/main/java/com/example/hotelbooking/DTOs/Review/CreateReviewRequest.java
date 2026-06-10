package com.example.hotelbooking.DTOs.Review;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateReviewRequest {
    @NotBlank
    private String bookingPublicId;
    @NotNull
    private BigDecimal rating;
    private String comment;
}