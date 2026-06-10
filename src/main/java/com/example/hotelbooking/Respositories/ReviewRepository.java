package com.example.hotelbooking.Respositories;

import com.example.hotelbooking.Models.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByPublicId(String publicId);
    boolean existsByBookingId(Long bookingId);
    List<Review> findAllByHotelId(Long hotelId);
    long countByHotelId(Long hotelId);

}
