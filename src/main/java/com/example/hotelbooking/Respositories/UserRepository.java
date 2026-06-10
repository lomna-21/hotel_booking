package com.example.hotelbooking.Respositories;

import com.example.hotelbooking.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    Optional<User> findByUsername(String username);

    @Query("SELECT DISTINCT u from User u LEFT JOIN FETCH u.roles")
    List<User> findAllUsersWithRoles();

    Optional<User> findByPublicId(String publicId);
}
