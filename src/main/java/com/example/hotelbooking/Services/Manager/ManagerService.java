package com.example.hotelbooking.Services.Manager;

import com.example.hotelbooking.DTOs.Booking.BookingResponse;
import com.example.hotelbooking.DTOs.Booking.BookingWithCustomerRequest;
import com.example.hotelbooking.DTOs.Booking.CustomerBookingRequest;
import com.example.hotelbooking.DTOs.CustomerProfile.CustomerProfileResponse;
import com.example.hotelbooking.DTOs.Hotel.HotelRequest;
import com.example.hotelbooking.DTOs.Hotel.HotelResponse;
import com.example.hotelbooking.DTOs.Manager.AssignManagerRequest;
import com.example.hotelbooking.DTOs.Manager.CreateManagerRequest;
import com.example.hotelbooking.DTOs.Manager.ManagerResponse;
import com.example.hotelbooking.DTOs.Payment.CreatePaymentRequest;
import com.example.hotelbooking.DTOs.Room.CreateRoomRequest;
import com.example.hotelbooking.DTOs.Room.RoomResponse;
import com.example.hotelbooking.ExceptionHandler.*;
import com.example.hotelbooking.Models.*;
import com.example.hotelbooking.Respositories.*;
import com.example.hotelbooking.Services.Booking.BookingLockService;
import com.example.hotelbooking.Services.Customer.CustomerService;
import com.example.hotelbooking.Services.Payment.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ManagerService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ManagerProfileRepository managerProfileRepository;
    private final HotelRepository hotelRepository;
    private final HotelManagerRepository hotelManagerRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoomRepository roomRepository;
    private final BookingLockService bookingLockService;
    private final CustomerProfileRepository customerProfileRepository;
    private final PaymentService paymentService;

    //    OWNER METHODS FOR CREATING MANAGING AND VIEWING MANAGERS
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ManagerResponse> createManager(CreateManagerRequest request) {

        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();

        User owner = userDetails.getUser();

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists");
        }

        Role managerRole = roleRepository.findByName("MANAGER")
                .orElseThrow(() -> new ResourceNotFoundException("MANAGER role not found"));

        User manager = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(new HashSet<>(Collections.singleton(managerRole)))
                .build();

        User savedManager = userRepository.save(manager);

        ManagerProfile profile = ManagerProfile.builder()
                .user(savedManager)
                .createdBy(owner)
                .build();

        managerProfileRepository.save(profile);

        return ResponseEntity.ok(toResponse(savedManager, profile));
    }

    @PreAuthorize("hasPermission(#hotelPublicId, 'hotel', 'assign:manager')")
    public ResponseEntity<String> assignManager(String hotelPublicId, AssignManagerRequest request) {

        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();

        User owner = userDetails.getUser();

        Hotel hotel = hotelRepository.findByPublicId(hotelPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        User manager = userRepository.findByPublicId(request.getManagerPublicId())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        // verify this manager was created by this owner
        if (!managerProfileRepository.existsByUserIdAndCreatedById(manager.getId(), owner.getId())) {
            throw new AccessDeniedException("You do not own this manager account");
        }

        // check if already assigned
        if (hotelManagerRepository.findByHotel_IdAndUser_Id(hotel.getId(), manager.getId()).isPresent()) {
            throw new IllegalArgumentException("Manager already assigned to this hotel");
        }

        Role managerRole = roleRepository.findByName("MANAGER")
                .orElseThrow(() -> new ResourceNotFoundException("MANAGER role not found"));

        HotelManager hotelManager = HotelManager.builder()
                .hotel(hotel)
                .user(manager)
                .scopedRole(managerRole)
                .build();

        hotelManagerRepository.save(hotelManager);

        String successResponse = "Manager assigned to hotel " + hotel.getName();
        return ResponseEntity.ok(successResponse);
    }

    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<ManagerResponse>> getAllManagers() {

        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();

        Long ownerId = userDetails.getUserId();

        List<ManagerProfile> profiles = managerProfileRepository.findAllByCreatedById(ownerId);

        List<ManagerResponse> responses = profiles.stream()
                .map(p -> toResponse(p.getUser(), p))
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }


    //    MANAGER METHODS
    @PreAuthorize(("hasRole('MANAGER')"))
    public ResponseEntity<List<HotelResponse>> getHotels() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();

        User manager = userDetails.getUser();
        List<HotelManager> assignedHotel = hotelManagerRepository.findByUserId(manager.getId());

        if (assignedHotel.isEmpty()) {
            throw new NoHotelsAssignedException("No hotels assigned to you");
        }

        List<Long> hotelIds = assignedHotel.stream()
                .map(hm -> hm.getHotel().getId())
                .collect(Collectors.toList());
        List<Hotel> hotels = hotelRepository.findAllByIdIn(hotelIds);
        List<HotelResponse> responses = new ArrayList<>();

        for (Hotel hotel : hotels) {
            HotelResponse response = toResponse(hotel);
            responses.add(response);
        }
        return ResponseEntity.ok(responses);


    }

    @PreAuthorize("hasRole('MANAGER') and hasPermission(#hotelPublicId, 'hotel', 'view:room')")
    public ResponseEntity<List<RoomResponse>> getRooms(String hotelPublicId) {

        Hotel hotel = hotelRepository.findByPublicId(hotelPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        List<Room> rooms = roomRepository.findAllByHotel_Id(hotel.getId());

        List<RoomResponse> responses = rooms.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @PreAuthorize(
            "hasRole('MANAGER') and hasPermission(#hotelPublicId, 'hotel', 'add:room')"
    )
    public ResponseEntity<RoomResponse> createRoom (String hotelPublicId, CreateRoomRequest request){

        Optional<Hotel> hotel = hotelRepository.findByPublicId(hotelPublicId);
        if(!hotel.isPresent()){
            throw new HotelNotFoundException("Hotel not found");
        }
        if(roomRepository.existsByHotel_IdAndRoomNumber(hotel.get().getId(), request.getRoomNumber())){
            throw new RoomNumberExistsException("Room number already exists");
        }
        Room room = requestTo(request);
        room.setHotel(hotel.get());
        try {
            roomRepository.save(room);
            RoomResponse response = toResponse(room);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            System.out.println("Error "+e.getMessage());
            throw new RuntimeException(e.getMessage());
        }

    }

    @Transactional
    @PreAuthorize("hasRole('MANAGER') and hasPermission(#hotelPublicId, 'hotel', 'create:booking')")
    public ResponseEntity<BookingResponse> createWalkInBooking(String hotelPublicId, BookingWithCustomerRequest request){

        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        User user = userDetails.getUser();
        CustomerProfile customerProfile  = createWalkInCustomer(request.getEmail(),
                request.getPhone(), request.getFirstName(), request.getLastName()).getBody();

        Hotel hotel = hotelRepository.findByPublicId(hotelPublicId).orElseThrow(
                () -> new HotelNotFoundException("Hotel Not Found")
        );
        Long internalHotelId = hotel.getId();

        long roomCount = roomRepository.countAvailableRooms(internalHotelId, request.getRoomType());
        if ( roomCount == 0) {
            throw new NoRoomsAvailableException("No rooms are currently available for the selected type");
        }

        CustomerBookingRequest bookingRequest = CustomerBookingRequest.builder()
                .hotelPublicId(hotelPublicId).roomType(request.getRoomType())
                .checkIn(request.getCheckIn()).checkOut(request.getCheckOut()).build();
        final String bookingType = "WALK-IN";

        if( roomCount > 1){
            //            Pessimistic Locking Logic
            return bookingLockService.pessimisticLockBooking(user, customerProfile, hotel, bookingRequest, bookingType);
        }
        return bookingLockService.optimisticLockBooking(user, customerProfile, hotel, bookingRequest, bookingType);
    }

    @PreAuthorize("hasRole('MANAGER') and hasPermission(#paymentPublicId, 'payment', 'create:payment')")
    public ResponseEntity<String> makePayment(String paymentPublicId, CreatePaymentRequest request){

        return paymentService.makeOnlinePayment(paymentPublicId,request);
    }

    @Transactional
    private ResponseEntity<CustomerProfile> createWalkInCustomer(String email,  String phone,
                                                                 String firstName, String lastName){

        if(customerProfileRepository.existsByPhone(phone) ||
                customerProfileRepository.existsByEmail(email)){
            throw new CustomerPhoneAlreadyExistsException("Phone number or Email is linked to some other customer");
        }
        CustomerProfile customer = CustomerProfile.builder().email(email).phone(phone).firstName(firstName)
                .lastName(lastName).build();
        CustomerProfile savedCustomer = customerProfileRepository.save(customer);
        return ResponseEntity.ok(savedCustomer);
    }

    private Room requestTo(CreateRoomRequest request){

        return Room.builder().roomNumber(request.getRoomNumber()).description(request.getDescription())
                .pricePerNight(request.getPricePerNight()).maxOccupancy(request.getMaxOccupancy())
                .roomType(request.getRoomType()).roomStatus(request.getRoomStatus()).build();
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

    private ManagerResponse toResponse(User user, ManagerProfile profile) {
        return ManagerResponse.builder()
                .publicId(user.getPublicId())
                .username(user.getUsername())
                .email(user.getEmail())
                .createdAt(profile.getCreatedAt())
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
