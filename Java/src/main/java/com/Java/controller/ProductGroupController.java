package com.Java.controller;

import com.Java.dto.BuildingGroupResponse;
import com.Java.service.DuckDBService;
import com.Java.service.FilterConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductGroupController {

    @Autowired
    private DuckDBService duckDBService;
    
    @Autowired
    private FilterConfigurationService filterConfigurationService;

    /**
     * Get all products grouped by building (no filters)
     */
    @GetMapping("/grouped-by-building")
    public ResponseEntity<List<BuildingGroupResponse>> getProductsGroupedByBuilding() {
        return getProductsGroupedByBuilding(new DuckDBService.ProductFilterRequest());
    }

    /**
     * Get filtered products grouped by building
     */
    @PostMapping("/grouped-by-building")
    public ResponseEntity<List<BuildingGroupResponse>> getProductsGroupedByBuilding(
            @RequestBody DuckDBService.ProductFilterRequest filterRequest) {
        try {
            System.out.println("Applying filters: " + filterRequest);
            
            List<Map<String, Object>> data = duckDBService.getProductsWithFiltersOptimized(filterRequest);
            List<BuildingGroupResponse> response = groupProductsByBuilding(data);
            
            System.out.println("Returning " + response.size() + " building groups");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error in getProductsGroupedByBuilding: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get role-specific filter configuration
     * Updated to use user roles instead of client IDs
     */
    @GetMapping("/filters")
    public ResponseEntity<List<String>> getFilterConfiguration(
            @RequestParam(defaultValue = "pricing_manager") String userRole) {
        try {
            List<String> filters = filterConfigurationService.getFiltersForRole(userRole);
            return ResponseEntity.ok(filters);
        } catch (Exception e) {
            System.err.println("Error getting filter configuration: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get available user roles
     */
    @GetMapping("/filters/available-roles")
    public ResponseEntity<List<UserRole>> getAvailableRoles() {
        List<UserRole> roles = Arrays.asList(
            new UserRole("pricing_manager", "Pricing Manager"),
            new UserRole("regional_manager", "Regional Manager"),
            new UserRole("reporting_user", "Reporting User")
        );
        return ResponseEntity.ok(roles);
    }

    // Simple helper class for role info
    public static class UserRole {
        private String role;
        private String displayName;

        public UserRole(String role, String displayName) {
            this.role = role;
            this.displayName = displayName;
        }

        // Getters and setters
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
    }

    private List<BuildingGroupResponse> groupProductsByBuilding(List<Map<String, Object>> data) {
        System.out.println("Processing " + data.size() + " records");

        Map<String, List<Map<String, Object>>> groupedByBuilding = data.stream()
                .collect(Collectors.groupingBy(row -> (String) row.get("building_name")));

        return groupedByBuilding.entrySet().stream()
                .map(entry -> {
                    String buildingName = entry.getKey();
                    List<BuildingGroupResponse.ProductSummary> products = entry.getValue().stream()
                            .map(this::mapToProductSummary)
                            .collect(Collectors.toList());

                    return new BuildingGroupResponse(buildingName, products);
                })
                .sorted(Comparator.comparing(BuildingGroupResponse::getBuildingName))
                .collect(Collectors.toList());
    }

    private BuildingGroupResponse.ProductSummary mapToProductSummary(Map<String, Object> row) {
        BigDecimal currentPrice = row.get("current_price") != null ?
                new BigDecimal(row.get("current_price").toString()) : BigDecimal.ZERO;

        Double bookingRate = row.get("booking_rate") != null ?
                Double.parseDouble(row.get("booking_rate").toString()) : 0.5;

        // Smart pricing based on demand
        BigDecimal multiplier;
        if (bookingRate >= 0.8) {
            multiplier = BigDecimal.valueOf(1.20); // High demand: +20%
        } else if (bookingRate >= 0.6) {
            multiplier = BigDecimal.valueOf(1.10); // Good demand: +10%
        } else if (bookingRate >= 0.4) {
            multiplier = BigDecimal.valueOf(1.05); // Medium demand: +5%
        } else {
            multiplier = BigDecimal.valueOf(0.95); // Low demand: -5%
        }

        BigDecimal recommendedPrice = currentPrice.multiply(multiplier);

        return new BuildingGroupResponse.ProductSummary(
                (String) row.get("product_id"),
                (String) row.get("room_name"),
                row.get("beds") != null ? Integer.parseInt(row.get("beds").toString()) : 0,
                (String) row.get("room_type"),
                (String) row.get("private_pool"),
                currentPrice,
                recommendedPrice,
                (String) row.get("currency")
        );
    }
}