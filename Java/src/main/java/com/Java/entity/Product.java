package com.Java.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "products")
public class Product {
    @Id
    private String id;

    @Column(name = "room_name")
    private String roomName;

    @Column(name = "arrival_date")
    private LocalDate arrivalDate;

    private Integer beds;

    @Column(name = "room_type")
    private String roomType;

    private Integer grade;

    @Column(name = "private_pool")
    private Boolean privatePool;

    @Column(name = "building_name")
    private String buildingName;

    // Constructors
    public Product() {}

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    public LocalDate getArrivalDate() { return arrivalDate; }
    public void setArrivalDate(LocalDate arrivalDate) { this.arrivalDate = arrivalDate; }
    public Integer getBeds() { return beds; }
    public void setBeds(Integer beds) { this.beds = beds; }
    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }
    public Integer getGrade() { return grade; }
    public void setGrade(Integer grade) { this.grade = grade; }
    public Boolean getPrivatePool() { return privatePool; }
    public void setPrivatePool(Boolean privatePool) { this.privatePool = privatePool; }
    public String getBuildingName() { return buildingName; }
    public void setBuildingName(String buildingName) { this.buildingName = buildingName; }
}
