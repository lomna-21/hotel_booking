package com.example.hotelbooking.Respositories;

import com.example.hotelbooking.Models.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoomStatusRepository extends JpaRepository<RoomStatus, Long> {
    Optional<RoomStatus> findByName(String name);
}
