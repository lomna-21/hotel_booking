package com.example.hotelbooking.Configs;

import java.io.Serializable;

public interface ResourcePermissionEvaluator {
    String getResourceType();
    boolean hasPermission(Long userId, Serializable targetId, String action);
    boolean hasPermission(Long userId, Object targetDomainObject, String action);
}