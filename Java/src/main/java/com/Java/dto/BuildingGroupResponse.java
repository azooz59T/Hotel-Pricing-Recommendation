package com.Java.dto;

import java.math.BigDecimal;
import java.util.List;

public class BuildingGroupResponse {
    private String buildingName;
    private List<ProductSummary> products;

    public static class ProductSummary {
        private String productId;
        private String roomName;
        private Integer beds;
        private String roomType;
        private String privatePool;
        private BigDecimal currentPrice;
        private BigDecimal recommendedPrice;
        private String currency;

        // Constructor
        public ProductSummary(String productId, String roomName, Integer beds,
                              String roomType, String privatePool, BigDecimal currentPrice,
                              BigDecimal recommendedPrice, String currency) {
            this.productId = productId;
            this.roomName = roomName;
            this.beds = beds;
            this.roomType = roomType;
            this.privatePool = privatePool;
            this.currentPrice = currentPrice;
            this.recommendedPrice = recommendedPrice;
            this.currency = currency;
        }

        // Getters and setters
        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        public String getRoomName() { return roomName; }
        public void setRoomName(String roomName) { this.roomName = roomName; }
        public Integer getBeds() { return beds; }
        public void setBeds(Integer beds) { this.beds = beds; }
        public String getRoomType() { return roomType; }
        public void setRoomType(String roomType) { this.roomType = roomType; }
        public String getPrivatePool() { return privatePool; }
        public void setPrivatePool(String privatePool) { this.privatePool = privatePool; }
        public BigDecimal getCurrentPrice() { return currentPrice; }
        public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }
        public BigDecimal getRecommendedPrice() { return recommendedPrice; }
        public void setRecommendedPrice(BigDecimal recommendedPrice) { this.recommendedPrice = recommendedPrice; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
    }

    // Constructor
    public BuildingGroupResponse(String buildingName, List<ProductSummary> products) {
        this.buildingName = buildingName;
        this.products = products;
    }

    // Getters and setters
    public String getBuildingName() { return buildingName; }
    public void setBuildingName(String buildingName) { this.buildingName = buildingName; }
    public List<ProductSummary> getProducts() { return products; }
    public void setProducts(List<ProductSummary> products) { this.products = products; }
}