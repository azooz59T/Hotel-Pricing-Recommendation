package com.Java.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.sql.*;
import java.util.*;

@Service
public class DuckDBService {

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    private Connection connection;

    public DuckDBService() {
        try {
            this.connection = DriverManager.getConnection("jdbc:duckdb:");
            setupAWS();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize DuckDB", e);
        }
    }

    private void setupAWS() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("SET s3_region='eu-north-1'");
            stmt.execute("SET s3_access_key_id='" + System.getenv("AWS_ACCESS_KEY_ID") + "'");
            stmt.execute("SET s3_secret_access_key='" + System.getenv("AWS_SECRET_ACCESS_KEY") + "'");
        }
    }

    public List<Map<String, Object>> getProductsWithPricesGroupedByBuilding() {
        String sql = """
        SELECT 
            b.Building as building_name,
            p.Id as product_id,
            p.room_name as room_name,
            p."no._of_beds" as beds,
            p.room_type as room_type,
            p.private_pool as private_pool,
            pr.Price as current_price,
            pr.Currency as currency
        FROM read_parquet('s3://%s/products/*.parquet') p
        JOIN read_parquet('s3://%s/buildings/*.parquet') b ON p.Id = b.product_id
        LEFT JOIN read_parquet('s3://%s/prices/*.parquet') pr ON p.Id = pr.product_id
        ORDER BY b.Building, p.room_name
        """.formatted(bucketName, bucketName, bucketName);

        return executeQuery(sql);
    }

    private List<Map<String, Object>> executeQuery(String sql) {
        List<Map<String, Object>> results = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnName(i), rs.getObject(i));
                }
                results.add(row);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Query failed: " + sql, e);
        }

        return results;
    }
}