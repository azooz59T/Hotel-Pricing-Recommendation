package com.Java.controller;

import com.Java.dto.BuildingGroupResponse;
import com.Java.service.DuckDBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductGroupController {

    @Autowired
    private DuckDBService duckDBService;

    @GetMapping("/grouped-by-building")
    public ResponseEntity<List<BuildingGroupResponse>> getProductsGroupedByBuilding() {
        try {
            System.out.println("Starting to fetch data from DuckDB...");

            List<Map<String, Object>> data = duckDBService.getProductsWithPricesAndMetrics();
            System.out.println("Fetched " + data.size() + " records from DuckDB");

            Map<String, List<Map<String, Object>>> groupedByBuilding = data.stream()
                    .collect(Collectors.groupingBy(row -> (String) row.get("building_name")));

            List<BuildingGroupResponse> response = groupedByBuilding.entrySet().stream()
                    .map(entry -> {
                        String buildingName = entry.getKey();
                        List<BuildingGroupResponse.ProductSummary> products = entry.getValue().stream()
                                .map(this::mapToProductSummary)
                                .collect(Collectors.toList());

                        return new BuildingGroupResponse(buildingName, products);
                    })
                    .collect(Collectors.toList());

            System.out.println("Returning " + response.size() + " building groups");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error in getProductsGroupedByBuilding: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    private BuildingGroupResponse.ProductSummary mapToProductSummary(Map<String, Object> row) {
        BigDecimal currentPrice = row.get("current_price") != null ?
                new BigDecimal(row.get("current_price").toString()) : BigDecimal.ZERO;

        // Get booking rate from cluster analysis
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
                Integer.parseInt(row.get("beds").toString()),
                (String) row.get("room_type"),
                (String) row.get("private_pool"),
                currentPrice,
                recommendedPrice,
                (String) row.get("currency")
        );
    }
}