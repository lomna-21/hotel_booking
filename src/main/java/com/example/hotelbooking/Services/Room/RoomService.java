package com.example.hotelbooking.Services.Room;

import com.example.hotelbooking.DTOs.Room.CreateRoomRequest;
import com.example.hotelbooking.DTOs.Room.RoomResponse;
import com.example.hotelbooking.ExceptionHandler.ResourceNotFoundException;
import com.example.hotelbooking.Models.Hotel;
import com.example.hotelbooking.Models.Room;
import com.example.hotelbooking.Models.RoomStatus;
import com.example.hotelbooking.Models.RoomType;
import com.example.hotelbooking.Respositories.HotelRepository;
import com.example.hotelbooking.Respositories.RoomRepository;
import com.example.hotelbooking.Respositories.RoomStatusRepository;
import com.example.hotelbooking.Respositories.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final RoomStatusRepository roomStatusRepository;

    @Transactional
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER') and hasPermission(#hotelPublicId, 'hotel', 'add:room')")
    public ResponseEntity<RoomResponse> createRoom(String hotelPublicId, CreateRoomRequest request) {

        Hotel hotel = hotelRepository.findByPublicId(hotelPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        // validate roomType exists in lookup table
        roomTypeRepository.findByName(request.getRoomType())
                .orElseThrow(() -> new IllegalArgumentException("Invalid room type: " + request.getRoomType()));

        // validate roomStatus exists in lookup table
        roomStatusRepository.findByName(request.getRoomStatus())
                .orElseThrow(() -> new IllegalArgumentException("Invalid room status: " + request.getRoomStatus()));

        if (roomRepository.existsByHotel_IdAndRoomNumber(hotel.getId(), request.getRoomNumber())) {
            throw new IllegalArgumentException("Room number already exists in this hotel");
        }

        Room room = Room.builder()
                .hotel(hotel)
                .roomNumber(request.getRoomNumber())
                .roomType(request.getRoomType().toUpperCase())
                .roomStatus(request.getRoomStatus().toUpperCase())
                .maxOccupancy(request.getMaxOccupancy())
                .description(request.getDescription())
                .pricePerNight(request.getPricePerNight())
                .build();

        Room saved = roomRepository.save(room);
        return ResponseEntity.ok(toResponse(saved));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER') and hasPermission(#hotelPublicId, 'hotel', 'add:room')")
    public ResponseEntity<List<RoomResponse>> createRooms(String hotelPublicId, List<CreateRoomRequest> requests) {

        Hotel hotel = hotelRepository.findByPublicId(hotelPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        List<RoomResponse> responses = new ArrayList<>();

        for (CreateRoomRequest request : requests) {

            // validate roomType
            roomTypeRepository.findByName(request.getRoomType())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid room type: " + request.getRoomType()));

            // validate roomStatus
            roomStatusRepository.findByName(request.getRoomStatus())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid room status: " + request.getRoomStatus()));

            // check duplicate room number in same hotel
            if (roomRepository.existsByHotel_IdAndRoomNumber(hotel.getId(), request.getRoomNumber())) {
                throw new IllegalArgumentException("Room number " + request.getRoomNumber() + " already exists in this hotel");
            }

            Room room = Room.builder()
                    .hotel(hotel)
                    .roomNumber(request.getRoomNumber())
                    .roomType(request.getRoomType())
                    .roomStatus(request.getRoomStatus())
                    .maxOccupancy(request.getMaxOccupancy())
                    .description(request.getDescription())
                    .pricePerNight(request.getPricePerNight())
                    .build();

            Room saved = roomRepository.save(room);
            responses.add(toResponse(saved));
        }

        return ResponseEntity.ok(responses);
    }

    private RoomResponse toResponse(Room room) {
        return RoomResponse.builder()
                .publicId(room.getPublicId())
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
