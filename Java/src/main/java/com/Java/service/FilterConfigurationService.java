package com.Java.service;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class FilterConfigurationService {

    /**
     * Simple role-based filter configurations
     * Just the essential filters needed to demonstrate the concept
     */
    private final Map<String, List<String>> roleFilters = Map.of(
            "pricing_manager", Arrays.asList("building", "room_type", "beds", "grade"),
            "regional_manager", Arrays.asList("building", "room_type"),
            "reporting_user", Arrays.asList("building", "room_type", "currency")
    );

    /**
     * Get available filters for a role
     */
    public List<String> getFiltersForRole(String userRole) {
        // Handle null/empty roles explicitly
        if (userRole == null || userRole.trim().isEmpty()) {
            userRole = "pricing_manager";
        }

        return roleFilters.getOrDefault(userRole, roleFilters.get("pricing_manager"));
    }

    /**
     * Get available roles
     */
    public List<String> getAvailableRoles() {
        return new ArrayList<>(roleFilters.keySet());
    }
}