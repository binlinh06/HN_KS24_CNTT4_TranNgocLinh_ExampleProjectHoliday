package com.phobo.management.security;

import com.phobo.management.entity.EmployeeProfile;
import com.phobo.management.repository.EmployeeProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service("employeeAuthService")
@RequiredArgsConstructor
@Slf4j
public class EmployeeAuthorizationService {

    private final EmployeeProfileRepository employeeProfileRepository;

    private EmployeeProfile getCurrentEmployeeProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserPrincipal)) {
            return null;
        }
        String userId = ((CustomUserPrincipal) principal).getId();
        return employeeProfileRepository.findByUserId(userId).orElse(null);
    }

    private boolean hasRole(String roleName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(roleName));
    }

    public boolean isManager() {
        return hasRole("ROLE_MANAGER");
    }

    public boolean isCashier() {
        if (hasRole("ROLE_MANAGER")) {
            return true;
        }
        if (hasRole("ROLE_ADMIN")) {
            return false; // Admin cannot operate store
        }
        if (!hasRole("ROLE_STAFF")) {
            return false;
        }
        EmployeeProfile employee = getCurrentEmployeeProfile();
        if (employee == null) {
            return false;
        }
        String position = employee.getPosition();
        if (position == null) {
            return false;
        }
        // Legacy fallback: generic "Staff" allows all staff actions
        if (position.equalsIgnoreCase("Staff")) {
            log.warn("[LEGACY FALLBACK] Employee {} has generic 'Staff' position. Allowing CASHIER role.", employee.getFullName());
            return true;
        }
        return position.equalsIgnoreCase("CASHIER") || position.equalsIgnoreCase("Cashier");
    }

    public boolean isKitchen() {
        if (hasRole("ROLE_MANAGER")) {
            return true;
        }
        if (hasRole("ROLE_ADMIN")) {
            return false;
        }
        if (!hasRole("ROLE_STAFF")) {
            return false;
        }
        EmployeeProfile employee = getCurrentEmployeeProfile();
        if (employee == null) {
            return false;
        }
        String position = employee.getPosition();
        if (position == null) {
            return false;
        }
        if (position.equalsIgnoreCase("Staff")) {
            log.warn("[LEGACY FALLBACK] Employee {} has generic 'Staff' position. Allowing KITCHEN role.", employee.getFullName());
            return true;
        }
        return position.equalsIgnoreCase("KITCHEN") || position.equalsIgnoreCase("Kitchen") || position.equalsIgnoreCase("Chef");
    }

    public boolean isWaiter() {
        if (hasRole("ROLE_MANAGER")) {
            return true;
        }
        if (hasRole("ROLE_ADMIN")) {
            return false;
        }
        if (!hasRole("ROLE_STAFF")) {
            return false;
        }
        EmployeeProfile employee = getCurrentEmployeeProfile();
        if (employee == null) {
            return false;
        }
        String position = employee.getPosition();
        if (position == null) {
            return false;
        }
        if (position.equalsIgnoreCase("Staff")) {
            log.warn("[LEGACY FALLBACK] Employee {} has generic 'Staff' position. Allowing WAITER role.", employee.getFullName());
            return true;
        }
        return position.equalsIgnoreCase("WAITER") || position.equalsIgnoreCase("Waiter") || position.equalsIgnoreCase("Server");
    }

    public boolean isStaffOrManager() {
        return hasRole("ROLE_MANAGER") || hasRole("ROLE_STAFF");
    }
}
