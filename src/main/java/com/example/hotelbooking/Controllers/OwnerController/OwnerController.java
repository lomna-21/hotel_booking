package com.example.hotelbooking.Controllers.OwnerController;


import com.example.hotelbooking.DTOs.AuthRegisterRequest;
import com.example.hotelbooking.DTOs.Hotel.HotelRequest;
import com.example.hotelbooking.DTOs.Hotel.HotelResponse;
import com.example.hotelbooking.DTOs.Manager.AssignManagerRequest;
import com.example.hotelbooking.DTOs.Manager.CreateManagerRequest;
import com.example.hotelbooking.DTOs.Manager.ManagerResponse;
import com.example.hotelbooking.DTOs.Room.CreateRoomRequest;
import com.example.hotelbooking.DTOs.Room.RoomResponse;
import com.example.hotelbooking.Models.CustomUserDetails;
import com.example.hotelbooking.Services.HotelService;
import com.example.hotelbooking.Services.Manager.ManagerService;
import com.example.hotelbooking.Services.Room.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/manage-hotels")
@RequiredArgsConstructor
@Tag(name = "Hotel owner API's", description = "Operations for hotel owners to manage or add hotels")
public class OwnerController {

    private final HotelService hotelService;
    private final ManagerService managerService;
    private final RoomService roomService;

    @Operation(summary = "Api for viewing all hotels(self)")
    @PreAuthorize("hasRole('OWNER')")
    @GetMapping("/list")
    public ResponseEntity<List<HotelResponse>> getAllHotels(@AuthenticationPrincipal CustomUserDetails userDetails){

        return hotelService.getAllHotels(userDetails);
    }
    @Operation(summary = "Api for creating new hotel")
    @PreAuthorize("hasRole('OWNER')")
    @PostMapping("/create")
    public ResponseEntity<HotelResponse> create (@Valid @RequestBody HotelRequest request){

        return hotelService.create(request);
    }

    @Operation(summary = "Api for updating details of hotel using put mapping")
    @PutMapping("/{publicId}")
    @PreAuthorize("hasPermission(#publicId, 'hotel', 'edit')")
    public ResponseEntity<HotelResponse> updateHotel(
            @PathVariable String publicId,
            @RequestBody @Valid HotelRequest request) {
        return hotelService.updateHotel(publicId, request);
    }

    @Operation(summary = "Api for updating details of hotel using patch mapping")
    @PatchMapping("/patch/{publicId}")
    @PreAuthorize("hasPermission(#publicId, 'hotel', 'edit')")
    public ResponseEntity<HotelResponse> patchHotel(
            @PathVariable String publicId,
            @RequestBody HotelRequest request) {
        return hotelService.patchHotel(publicId, request);
    }

    @Operation(summary = "Api for creating a manager account")
    @PreAuthorize("hasRole('OWNER')")
    @PostMapping("/manager/create")
    public ResponseEntity<ManagerResponse> managerCreate(@Valid @RequestBody CreateManagerRequest request){

        return managerService.createManager(request);
    }

    @Operation(summary = "Api for assigning a manager to a hotel")
    @PreAuthorize("hasRole('OWNER')")
    @PostMapping("/{hotelPublicId}/manager/assign")
    public ResponseEntity<String> assignManager (@PathVariable String hotelPublicId, @Valid @RequestBody AssignManagerRequest request){

        return managerService.assignManager(hotelPublicId, request);
    }


    @Operation(summary = "Api for viewing all managers")
    @PreAuthorize("hasRole('OWNER')")
    @GetMapping("/managers")
    public ResponseEntity<List<ManagerResponse>> getAllManagers(){

        return managerService.getAllManagers();
    }

    @Operation(summary = "Api for creating room in a hotel")
    @PostMapping("/{hotelPublicId}/create-room")
    public ResponseEntity<RoomResponse> createRoom(
            @PathVariable String hotelPublicId,
            @RequestBody @Valid CreateRoomRequest request) {
        return roomService.createRoom(hotelPublicId, request);
    }

    @Operation(summary = "Api for creating multiple rooms in a hotel")
    @PostMapping("/{hotelPublicId}/create-rooms")
    public ResponseEntity<List<RoomResponse>> createRooms(
            @PathVariable String hotelPublicId,
            @RequestBody @Valid List<CreateRoomRequest> request) {
        return roomService.createRooms(hotelPublicId, request);
    }

    @Operation(summary = "Api for viewing rooms in a hotel")
    @GetMapping("/{hotelPublicId}/rooms")
    public ResponseEntity<List<RoomResponse>> getRooms(@PathVariable String hotelPublicId) {
        return hotelService.getRooms(hotelPublicId);
    }

}
