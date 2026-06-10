package com.example.hotelbooking.Utils.PermissionEvaluator;

import com.example.hotelbooking.Configs.ResourcePermissionEvaluator;
import com.example.hotelbooking.Models.Payment;
import com.example.hotelbooking.Respositories.PaymentRepository;
import com.example.hotelbooking.Services.Permission.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@RequiredArgsConstructor
public class PaymentPermissionEvaluator implements ResourcePermissionEvaluator {

    private final PaymentRepository paymentRepository;
    private final PermissionService permissionService;

    @Override
    public String getResourceType() {
        return "payment";
    }

    @Override
    public boolean hasPermission(Long userId, Serializable targetId, String action) {
        Payment payment = paymentRepository.findByPublicId((String) targetId)
                .orElse(null);
        if (payment == null) return false;
        return permissionService.hasPaymentPermission(userId, payment, action);
    }

    @Override
    public boolean hasPermission(Long userId, Object targetDomainObject, String action) {
        if (targetDomainObject instanceof Payment) {
            Payment payment = (Payment) targetDomainObject;
            return permissionService.hasPaymentPermission(userId, payment, action);
        }
        return false;
    }
}
