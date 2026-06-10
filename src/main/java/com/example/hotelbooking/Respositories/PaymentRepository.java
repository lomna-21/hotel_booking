package com.example.hotelbooking.Respositories;

import com.example.hotelbooking.Models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment,Long> {

    Optional<Payment> findByBooking_Id(Long bookingId);
    @Modifying
    @Query("UPDATE Payment p SET p.status = :status WHERE p.publicId = :publicId")
    void updatePaymentStatusByPublicId(
            @Param("publicId") String publicId,
            @Param("status") String status
    );

    Optional<Payment> findByPublicId(String publicId);
}
