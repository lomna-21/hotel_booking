package com.example.hotelbooking.Configs;

import com.example.hotelbooking.Models.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomPermissionEvaluator implements PermissionEvaluator {


    private final List<ResourcePermissionEvaluator> evaluators;
    private final Map<String, ResourcePermissionEvaluator> evaluatorMap = new HashMap<>();

    @PostConstruct
    public void init() {
        for (ResourcePermissionEvaluator evaluator : evaluators) {
            evaluatorMap.put(evaluator.getResourceType(), evaluator);
        }
    }

//     Called when you pass the ID: hasPermission(#hotelId, 'hotel', 'edit')
//     Internally it checks available ResourcePermissionEvaluator which matches our resourceType
//     And the hasPermission of that particular class is invoked which in turn invokes permission service
//     which matches our permissions on db level

    @Override
    public boolean hasPermission(Authentication authentication,
                                 Serializable targetId,
                                 String targetType,
                                 Object permission) {
        Long userId = extractUserId(authentication);
        ResourcePermissionEvaluator evaluator = evaluatorMap.get(targetType);
        if (evaluator == null) return false;
        return evaluator.hasPermission(userId, targetId, permission.toString());
    }

    // Called when you pass the object: hasPermission(#hotel, 'edit')
    @Override
    public boolean hasPermission(Authentication authentication,
                                 Object targetDomainObject,
                                 Object permission) {
        Long userId = extractUserId(authentication);
        ResourcePermissionEvaluator evaluator = evaluatorMap.get(resolveType(targetDomainObject));
        if (evaluator == null) return false;
        return evaluator.hasPermission(userId, targetDomainObject, permission.toString());
    }

    private String resolveType(Object obj) {
        if (obj instanceof PermissionResource) {
            return ((PermissionResource) obj).getResourceType();
        }
        return null;
    }

    private Long extractUserId(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUserId();
    }
}