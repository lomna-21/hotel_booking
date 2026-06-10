package com.example.hotelbooking.Controllers.ManagerController;

import com.example.hotelbooking.DTOs.Booking.BookingResponse;
import com.example.hotelbooking.DTOs.Booking.BookingWithCustomerRequest;
import com.example.hotelbooking.DTOs.Hotel.HotelResponse;
import com.example.hotelbooking.DTOs.Payment.CreatePaymentRequest;
import com.example.hotelbooking.DTOs.Room.CreateRoomRequest;
import com.example.hotelbooking.DTOs.Room.RoomResponse;
import com.example.hotelbooking.Respositories.HotelRepository;
import com.example.hotelbooking.Services.HotelService;
import com.example.hotelbooking.Services.Manager.ManagerService;
import com.example.hotelbooking.Services.Room.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
@Tag(name = "Manager API's", description = "Operations for managers to manage their assigned hotels")
public class ManagerController {

    private final ManagerService managerService;

    @Operation(summary = "Api for manager to view all hotels assigned to them")
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/hotels-list")
    public ResponseEntity<List<HotelResponse>> getAllHotels(){

        return managerService.getHotels();
    }

    @Operation(summary = "Api for manager to view all rooms of an assigned hotel")
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/{hotelPublicId}/rooms")
    public ResponseEntity<List<RoomResponse>> getRooms(@PathVariable String hotelPublicId){

        return managerService.getRooms(hotelPublicId);
    }

    @Operation(summary = "Api for manager to create room in an assigned hotel")
    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/{hotelPublicId}/create-room")
    public ResponseEntity<RoomResponse> createRoom(@PathVariable String hotelPublicId,
                                                   @Valid @RequestBody CreateRoomRequest request){
        return managerService.createRoom(hotelPublicId,request);
    }

    @Operation(summary = "Api for manager to create booking in an assigned hotel")
    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/{hotelPublicId}/create-booking")
    public ResponseEntity<BookingResponse> createWalkInBooking(@PathVariable String hotelPublicId,
                                            @Valid @RequestBody BookingWithCustomerRequest request){

        return managerService.createWalkInBooking(hotelPublicId, request);
    }

    @Operation(summary = "Api for manager to process payment for the walk-in customer")
    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/{paymentPublicId}/process-payment")
    public ResponseEntity<String> processPayment(@PathVariable String paymentPublicId,
                                                 @Valid @RequestBody CreatePaymentRequest request){

        return managerService.makePayment(paymentPublicId, request);
    }


}
