package com.example.hotelbooking.Respositories;

import com.example.hotelbooking.Models.ManagerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ManagerProfileRepository extends JpaRepository<ManagerProfile, Long> {
    List<ManagerProfile> findAllByCreatedById(Long ownerId);
    boolean existsByUserIdAndCreatedById(Long userId, Long ownerId);
    Optional<ManagerProfile> findByUserId(Long userId);
}
