package com.example.hotelbooking.Services;

import com.example.hotelbooking.DTOs.Hotel.HotelRequest;
import com.example.hotelbooking.DTOs.Hotel.HotelResponse;
import com.example.hotelbooking.DTOs.Room.RoomResponse;
import com.example.hotelbooking.ExceptionHandler.ResourceNotFoundException;
import com.example.hotelbooking.Models.CustomUserDetails;
import com.example.hotelbooking.Models.Hotel;
import com.example.hotelbooking.Models.Room;
import com.example.hotelbooking.Models.User;
import com.example.hotelbooking.Respositories.HotelRepository;
import com.example.hotelbooking.Respositories.RoomRepository;
import com.example.hotelbooking.Respositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HotelService {

    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<HotelResponse> create(HotelRequest request){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails)authentication.getPrincipal();

        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(() ->
                new UsernameNotFoundException("User not found"));

        Hotel hotel = requestTo(request, user);
        try {
            Hotel saved = hotelRepository.save(hotel);
            HotelResponse response = HotelResponse.builder().name(saved.getName()).address(saved.getAddress())
                    .publicId(saved.getPublicId()).createdAt(saved.getCreatedAt())
                    .email(request.getEmail()).description(request.getDescription())
                    .phone(request.getPhone()).website(request.getWebsite()).build();
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            throw new RuntimeException("Unable to create hotel"+e.getMessage());
        }
    }

    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<HotelResponse>> getAllHotels(CustomUserDetails userDetails){

        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(() ->
                new UsernameNotFoundException("Username not found"));

        List<Hotel> hotels = hotelRepository.findAllByOwnerId(user.getId());

        List<HotelResponse> responses = new ArrayList<>();

        for(Hotel hotel : hotels){
            HotelResponse response = toResponse(hotel);
            responses.add(response);
        }
        return ResponseEntity.ok().body(responses);
    }

    @PreAuthorize("hasRole('OWNER') and hasPermission(#hotelPublicId, 'hotel', 'view:room')")
    public ResponseEntity<List<RoomResponse>> getRooms(String hotelPublicId) {

        Hotel hotel = hotelRepository.findByPublicId(hotelPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        List<Room> rooms = roomRepository.findAllByHotel_Id(hotel.getId());

        List<RoomResponse> responses = rooms.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @PreAuthorize("hasPermission(#publicId, 'hotel', 'edit')")
    public ResponseEntity<HotelResponse> updateHotel(String publicId, HotelRequest request) {

        Hotel hotel = hotelRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        // PUT replaces all fields
        hotel.setName(request.getName());
        hotel.setAddress(request.getAddress());
        hotel.setPhone(request.getPhone());
        hotel.setEmail(request.getEmail());
        hotel.setWebsite(request.getWebsite());
        hotel.setDescription(request.getDescription());

        Hotel saved = hotelRepository.save(hotel);
        return ResponseEntity.ok(toResponse(saved));
    }

    @PreAuthorize("hasPermission(#publicId, 'hotel', 'edit')")
    public ResponseEntity<HotelResponse> patchHotel(String publicId, HotelRequest request) {

        Hotel hotel = hotelRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        // PATCH only updates non-null fields
        if (request.getName() != null) hotel.setName(request.getName());
        if (request.getAddress() != null) hotel.setAddress(request.getAddress());
        if (request.getPhone() != null) hotel.setPhone(request.getPhone());
        if (request.getEmail() != null) hotel.setEmail(request.getEmail());
        if (request.getWebsite() != null) hotel.setWebsite(request.getWebsite());
        if (request.getDescription() != null) hotel.setDescription(request.getDescription());

        Hotel saved = hotelRepository.save(hotel);
        return ResponseEntity.ok(toResponse(saved));
    }

    private Hotel requestTo(HotelRequest request, User user){

        return Hotel.builder().name(request.getName()).address(request.getAddress()).owner(user)
                .description(request.getDescription()).phone(request.getPhone())
                .email(request.getEmail()).website(request.getWebsite()).build();
    }

    private HotelResponse toResponse(Hotel hotel) {
        return HotelResponse.builder()
                .publicId(hotel.getPublicId())
                .name(hotel.getName())
                .address(hotel.getAddress())
                .phone(hotel.getPhone())
                .email(hotel.getEmail())
                .website(hotel.getWebsite())
                .description(hotel.getDescription())
                .createdAt(hotel.getCreatedAt())
                .updatedAt(hotel.getUpdatedAt())
                .build();
    }

    private RoomResponse toResponse(Room room) {
        return RoomResponse.builder()
                .publicId(room.getPublicId())
                .hotelPublicId(room.getHotel().getPublicId())
                .roomNumber(room.getRoomNumber())
                .roomType(room.getRoomType())
                .roomStatus(room.getRoomStatus())
                .maxOccupancy(room.getMaxOccupancy())
                .description(room.getDescription())
                .pricePerNight(room.getPricePerNight())
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .build();
    }
}
