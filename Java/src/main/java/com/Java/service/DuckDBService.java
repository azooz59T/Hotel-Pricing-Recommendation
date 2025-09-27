package com.Java.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DuckDBService {

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        try {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName("org.duckdb.DuckDBDriver");
            dataSource.setUrl("jdbc:duckdb:");
            
            this.jdbcTemplate = new JdbcTemplate(dataSource);
            setupAWS();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize DuckDB", e);
        }
    }

    private void setupAWS() {
        jdbcTemplate.execute("SET s3_region='eu-north-1'");
        jdbcTemplate.execute("SET s3_access_key_id='" + System.getenv("AWS_ACCESS_KEY_ID") + "'");
        jdbcTemplate.execute("SET s3_secret_access_key='" + System.getenv("AWS_SECRET_ACCESS_KEY") + "'");
    }

    public String getBucketName() {
        return bucketName;
    }

    /**
     * Get products with optional filters using JdbcTemplate
     */
    public List<Map<String, Object>> getProductsWithFiltersOptimized(ProductFilterRequest filters) {
        String baseSql = """
        SELECT 
            b.Building as building_name,
            p.Id as product_id,
            p.room_name as room_name,
            p."no._of_beds" as beds,
            p.room_type as room_type,
            p.private_pool as private_pool,
            p.Grade as grade,
            p.arrival_date as arrival_date,
            pr.Price as current_price,
            pr.Currency as currency,
            cm.booking_rate as booking_rate
        FROM read_parquet('s3://%s/clustering/clustered_products/*.parquet') p
        JOIN read_parquet('s3://%s/buildings/*.parquet') b ON p.Id = b.product_id
        LEFT JOIN read_parquet('s3://%s/prices/*.parquet') pr ON p.Id = pr.product_id
        LEFT JOIN read_parquet('s3://%s/clustering/cluster_metrics/*.parquet') cm ON p.cluster_key = cm.cluster_key
        """.formatted(bucketName, bucketName, bucketName, bucketName);

        List<String> conditions = new ArrayList<>();
        
        // Build WHERE conditions
        addInCondition(conditions, "b.Building", filters.getBuildings());
        addInCondition(conditions, "p.room_type", filters.getRoomTypes());
        addInCondition(conditions, "p.\"no._of_beds\"", filters.getBeds());
        addInCondition(conditions, "p.Grade", filters.getGrades());
        addInCondition(conditions, "p.private_pool", filters.getPrivatePool());
        
        // Date range filters
        if (filters.getArrivalDateFrom() != null) {
            conditions.add("p.arrival_date >= '" + filters.getArrivalDateFrom() + "'");
        }
        if (filters.getArrivalDateTo() != null) {
            conditions.add("p.arrival_date <= '" + filters.getArrivalDateTo() + "'");
        }
        
        // Build final SQL
        String finalSql = baseSql;
        if (!conditions.isEmpty()) {
            finalSql += " WHERE " + String.join(" AND ", conditions);
        }
        finalSql += " ORDER BY b.Building, p.room_name";
        
        return jdbcTemplate.queryForList(finalSql);
    }

    /**
     * No filters version
     */
    public List<Map<String, Object>> getProductsWithPricesAndMetrics() {
        return getProductsWithFiltersOptimized(new ProductFilterRequest());
    }

    /**
     * Helper method to build IN clauses safely
     */
    private void addInCondition(List<String> conditions, String column, List<?> values) {
        if (isNotEmpty(values)) {
            String inClause = values.stream()
                .map(this::formatValue)
                .collect(Collectors.joining(",", column + " IN (", ")"));
            conditions.add(inClause);
        }
    }

    /**
     * Format value for SQL (handles strings vs numbers)
     */
    private String formatValue(Object value) {
        if (value instanceof String) {
            return "'" + value.toString().replace("'", "''") + "'";
        }
        return value.toString();
    }

    /**
     * Check if list is not null and not empty
     */
    private boolean isNotEmpty(List<?> list) {
        return list != null && !list.isEmpty();
    }

    /**
     * Execute arbitrary query (for filter options)
     */
    public List<Map<String, Object>> executeQuery(String sql) {
        return jdbcTemplate.queryForList(sql);
    }

    // Static class for filter request
    public static class ProductFilterRequest {
        private List<String> buildings;
        private List<String> roomTypes;
        private List<Integer> beds;
        private List<Integer> grades;
        private List<String> privatePool;
        private String arrivalDateFrom;
        private String arrivalDateTo;

        // Getters and setters
        public List<String> getBuildings() { return buildings; }
        public void setBuildings(List<String> buildings) { this.buildings = buildings; }
        public List<String> getRoomTypes() { return roomTypes; }
        public void setRoomTypes(List<String> roomTypes) { this.roomTypes = roomTypes; }
        public List<Integer> getBeds() { return beds; }
        public void setBeds(List<Integer> beds) { this.beds = beds; }
        public List<Integer> getGrades() { return grades; }
        public void setGrades(List<Integer> grades) { this.grades = grades; }
        public List<String> getPrivatePool() { return privatePool; }
        public void setPrivatePool(List<String> privatePool) { this.privatePool = privatePool; }
        public String getArrivalDateFrom() { return arrivalDateFrom; }
        public void setArrivalDateFrom(String arrivalDateFrom) { this.arrivalDateFrom = arrivalDateFrom; }
        public String getArrivalDateTo() { return arrivalDateTo; }
        public void setArrivalDateTo(String arrivalDateTo) { this.arrivalDateTo = arrivalDateTo; }
    }
}