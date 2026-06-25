package com.example.hotelbooking;

import com.example.hotelbooking.DTOs.AI.ChatRequest;
import com.example.hotelbooking.Models.*;
import com.example.hotelbooking.Respositories.*;
import com.example.hotelbooking.Services.AI.AiService;
import com.example.hotelbooking.Services.Gemini.GeminiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AiServiceTest {

    @Mock
    private GeminiService geminiService;

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private CustomerProfileRepository customerProfileRepository;

    @Mock
    private HotelManagerRepository hotelManagerRepository;

    @InjectMocks
    private AiService aiService;

    private User managerUser;
    private CustomUserDetails managerUserDetails;
    private Hotel hotel;
    private HotelManager hotelManager;

    @BeforeEach
    public void setup() {
        managerUser = User.builder()
                .id(1L)
                .username("testmanager")
                .publicId("USR-MGR")
                .build();

        managerUserDetails = new CustomUserDetails(managerUser);

        hotel = Hotel.builder()
                .id(10L)
                .publicId("HTL-TEST")
                .name("Test Hotel")
                .build();

        hotelManager = HotelManager.builder()
                .id(100L)
                .hotel(hotel)
                .user(managerUser)
                .build();

        // Mock Security Context
        Authentication authentication = mock(Authentication.class);
        lenient().when(authentication.getPrincipal()).thenReturn(managerUserDetails);
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void testGetManagerSummary_NoHotelsAssigned() {
        when(hotelManagerRepository.findByUserId(managerUser.getId())).thenReturn(Collections.emptyList());

        ChatRequest request = new ChatRequest();
        assertThrows(AccessDeniedException.class, () -> {
            aiService.getManagerSummary(request);
        });
    }

    @Test
    public void testGetManagerSummary_NotAssignedToSpecifiedHotel() {
        when(hotelManagerRepository.findByUserId(managerUser.getId())).thenReturn(Collections.singletonList(hotelManager));
        
        Hotel otherHotel = Hotel.builder()
                .id(11L)
                .publicId("HTL-OTHER")
                .name("Other Hotel")
                .build();
        when(hotelRepository.findByPublicId("HTL-OTHER")).thenReturn(Optional.of(otherHotel));

        ChatRequest request = new ChatRequest();
        request.setContextId("HTL-OTHER");

        assertThrows(AccessDeniedException.class, () -> {
            aiService.getManagerSummary(request);
        });
    }

    @Test
    public void testGetManagerSummary_Success() {
        when(hotelManagerRepository.findByUserId(managerUser.getId())).thenReturn(Collections.singletonList(hotelManager));
        
        LocalDate today = LocalDate.now();
        
        Room room = Room.builder()
                .id(1L)
                .roomType("DOUBLE")
                .build();

        Booking b1 = Booking.builder()
                .id(201L)
                .checkIn(today.withDayOfMonth(5))
                .bookingStatus("CONFIRMED")
                .totalAmount(new BigDecimal("2000.00"))
                .room(room)
                .build();

        Booking b2 = Booking.builder()
                .id(202L)
                .checkIn(today.withDayOfMonth(15))
                .bookingStatus("CANCELLED")
                .totalAmount(new BigDecimal("1500.00"))
                .room(room)
                .build();

        when(bookingRepository.findAllByHotelId(hotel.getId())).thenReturn(Arrays.asList(b1, b2));
        
        Review review = Review.builder()
                .id(301L)
                .rating(new BigDecimal("4.5"))
                .comment("Excellent stay")
                .createdAt(today.atStartOfDay())
                .build();
        when(reviewRepository.findAllByHotelId(hotel.getId())).thenReturn(Collections.singletonList(review));

        when(geminiService.generateResponse(anyString())).thenReturn("AI Generated Summary Output");

        ChatRequest request = new ChatRequest();
        String result = aiService.getManagerSummary(request);

        assertEquals("AI Generated Summary Output", result);
        verify(geminiService, times(1)).generateResponse(anyString());
    }

    @Test
    public void testGetOwnerReport_NoHotels() {
        when(hotelRepository.findAllByOwnerId(managerUser.getId())).thenReturn(Collections.emptyList());

        ChatRequest request = new ChatRequest();
        String result = aiService.getOwnerReport(request);

        assertEquals("You do not own any hotels currently, so there is no performance report to display.", result);
    }

    @Test
    public void testGetOwnerReport_Success() {
        when(hotelRepository.findAllByOwnerId(managerUser.getId())).thenReturn(Collections.singletonList(hotel));

        LocalDate today = LocalDate.now();
        Room room = Room.builder()
                .id(1L)
                .roomType("SINGLE")
                .build();

        Booking b = Booking.builder()
                .id(201L)
                .checkIn(today.withDayOfMonth(5))
                .bookingStatus("CONFIRMED")
                .totalAmount(new BigDecimal("1000.00"))
                .room(room)
                .build();

        when(bookingRepository.findAllByHotelId(hotel.getId())).thenReturn(Collections.singletonList(b));
        when(reviewRepository.findAllByHotelId(hotel.getId())).thenReturn(Collections.emptyList());
        when(geminiService.generateResponse(anyString())).thenReturn("AI Owner Report Output");

        ChatRequest request = new ChatRequest();
        String result = aiService.getOwnerReport(request);

        assertEquals("AI Owner Report Output", result);
        verify(geminiService, times(1)).generateResponse(anyString());
    }
}
