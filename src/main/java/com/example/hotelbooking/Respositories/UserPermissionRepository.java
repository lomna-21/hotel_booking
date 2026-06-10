package com.example.hotelbooking.Respositories;

import com.example.hotelbooking.Models.UserPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermission, Long> {

    // UserPermissionRepository
    @Query("SELECT up FROM UserPermission up " +
            "WHERE up.user.id = :userId " +
            "AND up.permission.action = :action " +
            "AND up.permission.resourceType = :resourceType " +
            "AND up.resourceId = :resourceId")
    Optional<UserPermission> findByUserIdAndActionAndResourceTypeAndResourceId(
            @Param("userId") Long userId,
            @Param("action") String action,
            @Param("resourceType") String resourceType,
            @Param("resourceId") Long resourceId);

    @Query("SELECT up FROM UserPermission up " +
            "WHERE up.user.id = :userId " +
            "AND up.permission.action = :action " +
            "AND up.permission.resourceType = :resourceType " +
            "AND up.resourceId IS NULL")
    Optional<UserPermission> findByUserIdAndActionAndResourceTypeAndResourceIdIsNull(
            @Param("userId") Long userId,
            @Param("action") String action,
            @Param("resourceType") String resourceType);
}
