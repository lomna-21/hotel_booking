package com.example.hotelbooking.Services.Permission;

import com.example.hotelbooking.Models.Booking;
import com.example.hotelbooking.Models.HotelManager;
import com.example.hotelbooking.Models.Payment;
import com.example.hotelbooking.Models.UserPermission;
import com.example.hotelbooking.Respositories.HotelManagerRepository;
import com.example.hotelbooking.Respositories.HotelRepository;
import com.example.hotelbooking.Respositories.UserPermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PermissionService {

    private final HotelRepository hotelRepository;
    private final HotelManagerRepository hotelManagerRepository;
    private final UserPermissionRepository userPermissionRepository;

    public boolean hasHotelPermission(Long userId, Long resourceId, String resourceType, String action) {

        // 1. Scoped direct override — highest priority, can explicitly grant OR revoke
        Optional<UserPermission> scopedOverride = userPermissionRepository
                .findByUserIdAndActionAndResourceTypeAndResourceId(userId, action, resourceType, resourceId);
        if (scopedOverride.isPresent()) {
            return scopedOverride.get().isGranted();
        }

        // 2. Global direct override
        Optional<UserPermission> globalOverride = userPermissionRepository
                .findByUserIdAndActionAndResourceTypeAndResourceIdIsNull(userId, action, resourceType);
        if (globalOverride.isPresent()) {
            return globalOverride.get().isGranted();
        }

        // 3. Ownership — owner can do anything to their own hotel
        if ("hotel".equals(resourceType)) {
            if (hotelRepository.existsByIdAndOwnerId(resourceId, userId)) {
                return true;
            }
        }

        // 4. Scoped role via hotel_managers — only applies to that specific hotel
//        return hotelManagerRepository.findByHotel_IdAndUser_Id(resourceId, userId)
//                .map(hm -> hm.getScopedRole().getPermissions().stream()
//                        .anyMatch(p -> p.getAction().equals(action)
//                                && p.getResourceType().equals(resourceType)))
//                .orElse(false);
        String[] parts = action.split(":");
        String actionVerb = parts[0];        // "add"
        String actionResource = parts[1];    // "room"

        return hotelManagerRepository.managerHasPermission(resourceId, userId, actionVerb, actionResource);
    }

    public boolean hasPaymentPermission(Long callerId, Payment payment, String action) {

        // 1. explicit override
        Optional<UserPermission> override = userPermissionRepository
                .findByUserIdAndActionAndResourceTypeAndResourceId(
                        callerId, action, "payment", payment.getId());
        if (override.isPresent()) return override.get().isGranted();

        // 2. customer owns the booking
        Booking booking = payment.getBooking();
        if (booking.getCustomer() != null &&
                booking.getCustomer().getUser() != null &&
                booking.getCustomer().getUser().getId().equals(callerId)) {
            return true;
        }

        // 3. owner of the hotel
        if (booking.getHotel().getOwner().getId().equals(callerId)) {
            return true;
        }

        // 4. manager of the hotel
        String[] parts = action.split(":");
        String actionVerb = parts[0];
        String actionResource = parts[1];
        return hotelManagerRepository.managerHasPermission(
                booking.getHotel().getId(), callerId, actionVerb, actionResource);
    }}
