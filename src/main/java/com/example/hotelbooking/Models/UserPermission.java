package com.example.hotelbooking.Models;

import lombok.*;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "user_permissions")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true, updatable = false)
    private String publicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false)
    @ToString.Exclude
    private Permission permission;

    @Column(name = "resource_id")
    private Long resourceId;

    @Column(name = "is_granted", nullable = false)
    private boolean granted;

    @Column(name = "reason")
    private String reason;

    @PrePersist
    protected void onCreate() {
        this.publicId = "UPM-" + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }
}
