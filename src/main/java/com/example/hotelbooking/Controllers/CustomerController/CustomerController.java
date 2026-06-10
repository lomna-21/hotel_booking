package com.example.hotelbooking.Controllers.CustomerController;

import com.example.hotelbooking.DTOs.Booking.BookingConfirmedResponse;
import com.example.hotelbooking.DTOs.Booking.BookingResponse;
import com.example.hotelbooking.DTOs.Booking.CustomerBookingRequest;
import com.example.hotelbooking.DTOs.Hotel.CustomerHotelResponse;
import com.example.hotelbooking.DTOs.Payment.CreatePaymentRequest;
import com.example.hotelbooking.DTOs.Review.CreateReviewRequest;
import com.example.hotelbooking.DTOs.Review.ReviewResponse;
import com.example.hotelbooking.DTOs.Room.CustomerRoomResponse;
import com.example.hotelbooking.Services.Customer.CustomerService;
import com.example.hotelbooking.Services.Payment.PaymentService;
import com.example.hotelbooking.Services.Review.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
@Tag(name = "Customer API's", description = "Operations for customers to view hotels,rooms and book")
public class CustomerController {

    private final CustomerService customerService;
    private final PaymentService paymentService;
    private final ReviewService reviewService;

    @Operation(summary = "Api for viewing all hotels")
    @GetMapping
    public ResponseEntity<List<CustomerHotelResponse>> getHotels(){

        return customerService.getHotels();
    }

    @Operation(summary = "Api for viewing rooms of a hotel(uses hotel's publicId)")
    @GetMapping("/{hotelPublicId}/rooms")
    public ResponseEntity<List<CustomerRoomResponse>> getRooms(@PathVariable String hotelPublicId){

        return customerService.getRooms(hotelPublicId);
    }

    @Operation(summary = "Api for booking room")
    @PostMapping("/book-room")
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody CustomerBookingRequest request){

        return customerService.customerBooking(request);
    }

    @Operation(summary = "Api for making payment for booking")
    @PostMapping("/{paymentPublicId}/pay")
    public ResponseEntity<String> makePayment(@PathVariable String paymentPublicId,
                                              @Valid @RequestBody CreatePaymentRequest request){

        return paymentService.makeOnlinePayment(paymentPublicId, request);
    }

    @Operation(summary = "Api for viewing all bookings(self)")
    @PostMapping("/bookings")
    public ResponseEntity<List<BookingConfirmedResponse>> getBookings(){

        return customerService.getBookings();
    }

    @Operation(summary = "Api for giving reviews to hotel")
    @PostMapping("/reviews")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ReviewResponse> createReview(
            @RequestBody @Valid CreateReviewRequest request) {
        return reviewService.createReview(request);
    }
}