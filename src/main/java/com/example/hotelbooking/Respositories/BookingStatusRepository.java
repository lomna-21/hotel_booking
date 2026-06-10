package com.example.hotelbooking.Respositories;

import com.example.hotelbooking.Models.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookingStatusRepository extends JpaRepository<BookingStatus, Long> {
    Optional<BookingStatus> findByName(String name);
}
