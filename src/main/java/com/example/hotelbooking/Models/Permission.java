package com.example.hotelbooking.Models;

import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static java.util.UUID.*;

@Entity
@Table(name = "permissions")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true, updatable = false)
    private String publicId;

    @Column(name = "name", nullable = false, unique = true)
    private String name; // e.g. "edit:hotel"

    @Column(name = "action", nullable = false)
    private String action; // e.g. "edit"

    @Column(name = "resource_type", nullable = false)
    private String resourceType; // e.g. "hotel"

    @ManyToMany(mappedBy = "permissions")
    @ToString.Exclude
    private Set<Role> roles = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        this.publicId = "PRM-" + randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Permission)) return false;
        Permission that = (Permission) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
