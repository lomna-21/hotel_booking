package com.example.hotelbooking.Services.Review;

import com.example.hotelbooking.DTOs.Review.CreateReviewRequest;
import com.example.hotelbooking.DTOs.Review.ReviewResponse;
import com.example.hotelbooking.ExceptionHandler.CustomerNotFoundException;
import com.example.hotelbooking.ExceptionHandler.ResourceNotFoundException;
import com.example.hotelbooking.Models.*;
import com.example.hotelbooking.Respositories.BookingRepository;
import com.example.hotelbooking.Respositories.CustomerProfileRepository;
import com.example.hotelbooking.Respositories.HotelRepository;
import com.example.hotelbooking.Respositories.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;


@Service
@RequiredArgsConstructor
public class ReviewService {

    private final CustomerProfileRepository customerProfileRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final HotelRepository hotelRepository;

    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ReviewResponse> createReview(CreateReviewRequest request) {

        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();

        // get customer profile
        CustomerProfile customer = customerProfileRepository
                .findByUserId(userDetails.getUser().getId())
                .orElseThrow(() -> new CustomerNotFoundException("Customer profile not found"));

        // find booking — ensures this customer made this booking
        Booking booking = bookingRepository
                .findByPublicIdAndCustomer_Id(request.getBookingPublicId(), customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // only completed bookings can be reviewed
        if (!booking.getBookingStatus().equals("COMPLETED")) {
            throw new IllegalArgumentException("You can only review after your stay is completed");
        }

        // one review per booking
        if (reviewRepository.existsByBookingId(booking.getId())) {
            throw new IllegalArgumentException("You have already reviewed this booking");
        }

        // validate rating
        if (request.getRating().compareTo(BigDecimal.ZERO) < 0 ||
                request.getRating().compareTo(new BigDecimal("5.0")) > 0) {
            throw new IllegalArgumentException("Rating must be between 0.0 and 5.0");
        }

        Review review = Review.builder()
                .booking(booking)
                .customer(customer)
                .hotel(booking.getHotel())
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        reviewRepository.save(review);

        // update hotel star rating
        updateHotelRating(booking.getHotel(), request.getRating());

        return ResponseEntity.ok(toResponse(review));
    }

    private void updateHotelRating(Hotel hotel, BigDecimal newRating) {
        long totalReviews = reviewRepository.countByHotelId(hotel.getId());

        // weighted average
        // newAverage = (currentRating * (totalReviews - 1) + newRating) / totalReviews
        BigDecimal currentRating = hotel.getStarRating() != null ?
                hotel.getStarRating() : BigDecimal.ZERO;

        BigDecimal totalReviewsBD = BigDecimal.valueOf(totalReviews);
        BigDecimal newAverage = currentRating
                .multiply(totalReviewsBD.subtract(BigDecimal.ONE))
                .add(newRating)
                .divide(totalReviewsBD, 1, RoundingMode.HALF_UP);

        hotel.setStarRating(newAverage);
        hotelRepository.save(hotel);
    }

    private ReviewResponse toResponse(Review review) {
        return ReviewResponse.builder()
                .publicId(review.getPublicId())
                .hotelPublicId(review.getHotel().getPublicId())
                .hotelName(review.getHotel().getName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
