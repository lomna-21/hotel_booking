package com.example.hotelbooking.Respositories;

import com.example.hotelbooking.Models.Hotel;
import com.example.hotelbooking.Models.HotelManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HotelManagerRepository extends JpaRepository<HotelManager,Long> {
    // HotelManagerRepository
    Optional<HotelManager> findByHotel_IdAndUser_Id(Long hotelId, Long userId);

    List<HotelManager> findByUserId(Long userId);

    @Query("SELECT COUNT(hm) > 0 FROM HotelManager hm " +
            "JOIN hm.scopedRole r " +
            "JOIN r.permissions p " +
            "WHERE hm.hotel.id = :hotelId " +
            "AND hm.user.id = :userId " +
            "AND p.action = :action " +
            "AND p.resourceType = :resourceType")
    boolean managerHasPermission(
            @Param("hotelId") Long hotelId,
            @Param("userId") Long userId,
            @Param("action") String action,
            @Param("resourceType") String resourceType
    );

}
