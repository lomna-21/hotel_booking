package com.example.hotelbooking.Respositories;

import com.example.hotelbooking.Models.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {

    @Query("SELECT h FROM Hotel h WHERE h.owner.id = :ownerId")
    List<Hotel> findAllByOwnerId(Long ownerId);

    boolean existsByIdAndOwnerId(Long id, Long ownerId);

    Optional<Hotel> findByPublicId(String publicId);

    List<Hotel> findAllByIdIn(List<Long> hotelIds);

    @Query("Select h.publicId FROM Hotel h where h.id= :id")
    String getPublicIdByHotelId(Long id);

}
