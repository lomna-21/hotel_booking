package com.example.hotelbooking.Utils.PermissionEvaluator;

import com.example.hotelbooking.Configs.PermissionResource;
import com.example.hotelbooking.Configs.ResourcePermissionEvaluator;
import com.example.hotelbooking.Models.Hotel;
import com.example.hotelbooking.Respositories.HotelRepository;
import com.example.hotelbooking.Respositories.PermissionRepository;
import com.example.hotelbooking.Services.Permission.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@RequiredArgsConstructor
public class HotelPermissionEvaluator implements ResourcePermissionEvaluator, PermissionResource {

    private final HotelRepository hotelRepository;
    private final PermissionService permissionService;

    @Override
    public String getResourceType() {
        return "hotel";
    }

    @Override
    public boolean hasPermission(Long userId, Serializable targetId, String action) {
        Long internalId = hotelRepository.findByPublicId((String) targetId)
                .map(Hotel::getId)
                .orElse(null);
        if (internalId==null) return false;
        return permissionService.hasHotelPermission(userId, internalId, "hotel", action);
    }

    @Override
    public boolean hasPermission(Long userId, Object targetDomainObject, String action) {
        if (targetDomainObject instanceof Hotel) {
            Hotel hotel = (Hotel) targetDomainObject;
            return permissionService.hasHotelPermission(userId, hotel.getId(), "hotel", action);
        }
        return false;
    }
}


