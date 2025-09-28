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

    @GetMapping("/multi-currency")
    public ResponseEntity<List<MultiCurrencyProductResponse>> getMultiCurrencyProducts(
            @RequestParam(defaultValue = "current_price") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            List<Map<String, Object>> data = duckDBService.getProductsWithFiltersOptimized(new DuckDBService.ProductFilterRequest());

            // Group by product ID, collect all currencies
            Map<String, MultiCurrencyProductResponse> productMap = new HashMap<>();

            for (Map<String, Object> row : data) {
                String productId = (String) row.get("product_id");
                String currency = (String) row.get("currency");

                if (!productMap.containsKey(productId)) {
                    MultiCurrencyProductResponse product = new MultiCurrencyProductResponse();
                    product.setProductId(productId);
                    product.setRoomName((String) row.get("room_name"));
                    product.setBuildingName((String) row.get("building_name"));
                    product.setPrices(new HashMap<>());
                    productMap.put(productId, product);
                }

                if (currency != null && row.get("current_price") != null) {
                    BigDecimal price = new BigDecimal(row.get("current_price").toString());
                    productMap.get(productId).getPrices().put(currency, price);
                }
            }

            List<MultiCurrencyProductResponse> result = new ArrayList<>(productMap.values());

            result.sort((a, b) -> {
                BigDecimal priceA = a.getPrices().getOrDefault(sortBy, BigDecimal.ZERO);
                BigDecimal priceB = b.getPrices().getOrDefault(sortBy, BigDecimal.ZERO);

                // Handle null prices (products without that currency)
                if (priceA.equals(BigDecimal.ZERO) && !priceB.equals(BigDecimal.ZERO)) {
                    return 1; // Put products without price at the end
                }
                if (!priceA.equals(BigDecimal.ZERO) && priceB.equals(BigDecimal.ZERO)) {
                    return -1; // Put products with price first
                }

                return "desc".equals(sortDirection) ? priceB.compareTo(priceA) : priceA.compareTo(priceB);
            });

            // pagination
            int start = page * size;
            int end = Math.min(start + size, result.size());
            List<MultiCurrencyProductResponse> paginatedResult = result.subList(start, end);

            return ResponseEntity.ok(paginatedResult);
        } catch (Exception e) {
            System.err.println("Error in getMultiCurrencyProducts: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // MultiCurrency response class
    public static class MultiCurrencyProductResponse {
        private String productId;
        private String roomName;
        private String buildingName;
        private Map<String, BigDecimal> prices = new HashMap<>();

        // Getters and setters
        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        public String getRoomName() { return roomName; }
        public void setRoomName(String roomName) { this.roomName = roomName; }
        public String getBuildingName() { return buildingName; }
        public void setBuildingName(String buildingName) { this.buildingName = buildingName; }
        public Map<String, BigDecimal> getPrices() { return prices; }
        public void setPrices(Map<String, BigDecimal> prices) { this.prices = prices; }
    }
}