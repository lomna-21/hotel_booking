package com.example.hotelbooking.Models;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "hotel_managers",
        uniqueConstraints = @UniqueConstraint(columnNames = {"hotel_id", "user_id"})
)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HotelManager {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true, updatable = false)
    private String publicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    @ToString.Exclude
    private Hotel hotel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scoped_role_id", nullable = false)
    @ToString.Exclude
    private Role scopedRole;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private LocalDateTime assignedAt;

    @PrePersist
    protected void onCreate() {
        this.publicId = "HMG-" + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
        assignedAt = LocalDateTime.now();
    }
}
