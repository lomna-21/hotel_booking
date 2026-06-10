package com.example.hotelbooking.Respositories;

import com.example.hotelbooking.Models.CustomerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, Long> {
    Optional<CustomerProfile> findByPublicId(String publicId);
    Optional<CustomerProfile> findByUserId(Long userId);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
}
