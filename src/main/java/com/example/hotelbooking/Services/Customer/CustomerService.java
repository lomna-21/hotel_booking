package com.example.hotelbooking.Services.Customer;

import com.example.hotelbooking.DTOs.Booking.BookingConfirmedResponse;
import com.example.hotelbooking.DTOs.Booking.BookingResponse;
import com.example.hotelbooking.DTOs.Booking.CustomerBookingRequest;
import com.example.hotelbooking.DTOs.CustomerProfile.CustomerProfileResponse;
import com.example.hotelbooking.DTOs.Hotel.CustomerHotelResponse;
import com.example.hotelbooking.DTOs.Room.CustomerRoomResponse;
import com.example.hotelbooking.ExceptionHandler.*;
import com.example.hotelbooking.Models.*;
import com.example.hotelbooking.Respositories.*;
import com.example.hotelbooking.Services.Booking.BookingLockService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final BookingLockService bookingLockService;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;

    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<CustomerHotelResponse>> getHotels (){

        try {
            List<Hotel> hotels = hotelRepository.findAll();

            List<CustomerHotelResponse> responses = hotels.stream().map(this::toCustomerResponse).collect(Collectors.toList());

            return ResponseEntity.ok().body(responses);
        }catch (Exception ex){
            throw  new RuntimeException("Unexpected error occurred");
        }
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<CustomerRoomResponse>> getRooms(String hotelPublicId){

        Hotel hotel = hotelRepository.findByPublicId(hotelPublicId).orElseThrow( ()
                -> new HotelNotFoundException("Hotel not found")
        );

        List<Room> rooms = roomRepository.findAllByHotel_Id(hotel.getId());

        if (rooms.isEmpty()) {
            throw new NoRoomsFoundException("No Rooms Found");
        }

        Map<String, List<Room>> groupedByType = rooms.stream()
                .collect(Collectors.groupingBy(Room::getRoomType));

        List<CustomerRoomResponse> responses = groupedByType.entrySet().stream()
                .map(entry -> {
                    List<Room> roomsOfType = entry.getValue();
                    Room sample = roomsOfType.get(0);

                    long availableCount = roomsOfType.stream()
                            .filter(r -> r.getRoomStatus().equals("AVAILABLE"))
                            .count();

                    return CustomerRoomResponse.builder()
                            .publicId(sample.getPublicId())
                            .hotelPublicId(hotelPublicId)
                            .roomType(entry.getKey())
                            .roomStatus(sample.getRoomStatus())
                            .maxOccupancy(sample.getMaxOccupancy())
                            .description(sample.getDescription())
                            .pricePerNight(sample.getPricePerNight())
                            .availableRooms(availableCount)
                            .build();
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<BookingResponse> customerBooking(CustomerBookingRequest request) {

        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();

        User user = userDetails.getUser();

        CustomerProfile customerProfile = customerProfileRepository.findByUserId(user.getId()).orElseThrow(
                () -> new CustomerNotFoundException("Customer not found for the user")
        );

        Hotel hotel = hotelRepository.findByPublicId(request.getHotelPublicId()).orElseThrow(
                () -> new HotelNotFoundException("Hotel Not Found")
        );
        Long internalHotelId = hotel.getId();
        String bookingType = "ONLINE";

        long roomCount = roomRepository.countAvailableRooms(internalHotelId, request.getRoomType());
        if ( roomCount == 0) {
            throw new RuntimeException();
        }
        if( roomCount > 1){
            //            Pessimistic Locking Logic
            return bookingLockService.pessimisticLockBooking(user, customerProfile, hotel, request, bookingType);
        }
            return bookingLockService.optimisticLockBooking(user, customerProfile, hotel, request, bookingType);

    }

    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<BookingConfirmedResponse>> getBookings() {

        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();

        List<Booking> bookings = bookingRepository.findAllByBookedBy_Id(userDetails.getUser().getId());

        if (bookings.isEmpty()) {
            throw new ResourceNotFoundException("No bookings found");
        }

        List<BookingConfirmedResponse> responses = bookings.stream()
                .map(booking -> {
                    Payment payment = paymentRepository.findByBooking_Id(booking.getId())
                            .orElse(null);

                    String paymentId = payment != null ? payment.getPublicId() : "N/A";


                    return BookingConfirmedResponse.builder()
                            .bookingId(booking.getPublicId())
                            .paymentId(paymentId)
                            .checkin(booking.getCheckIn())
                            .checkOut(booking.getCheckOut())
                            .build();
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    private CustomerHotelResponse toCustomerResponse(Hotel hotel) {
        return CustomerHotelResponse.builder()
                .publicId(hotel.getPublicId())
                .name(hotel.getName())
                .rating(hotel.getStarRating())
                .address(hotel.getAddress())
                .phone(hotel.getPhone())
                .email(hotel.getEmail())
                .website(hotel.getWebsite())
                .description(hotel.getDescription())
                .build();
    }

    private CustomerProfileResponse toCustomerResponse(CustomerProfile customer){
        return CustomerProfileResponse.builder()
                .email(customer.getEmail()).phone(customer.getPhone())
                .firstName(customer.getFirstName()).lastName(customer.getLastName()).build();
    }

    private CustomerRoomResponse toRoomResponse(Room room) {
        return CustomerRoomResponse.builder()
                .publicId(room.getPublicId())
                .roomType(room.getRoomType())
                .roomStatus(room.getRoomStatus())
                .maxOccupancy(room.getMaxOccupancy())
                .description(room.getDescription())
                .pricePerNight(room.getPricePerNight())
                .build();
    }

    private BookingResponse toBookingResponse(Booking booking, Payment payment) {
        return BookingResponse.builder()
                .publicId(booking.getPublicId())
                .hotelPublicId(booking.getHotel().getPublicId())
                .roomPublicId(booking.getRoom().getPublicId())
                .roomType(booking.getRoom().getRoomType())
                .firstName(booking.getFirstName())
                .lastName(booking.getLastName())
                .phone(booking.getPhone())
                .email(booking.getEmail())
                .checkIn(booking.getCheckIn())
                .checkOut(booking.getCheckOut())
                .totalAmount(booking.getTotalAmount())
                .paymentPublicId(payment.getPublicId())
                .bookingStatus(booking.getBookingStatus())
                .bookingType(booking.getBookingType())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}
