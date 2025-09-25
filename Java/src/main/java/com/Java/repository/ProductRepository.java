package com.Java.repository;

import com.Java.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

    @Query("SELECT DISTINCT p.buildingName FROM Product p ORDER BY p.buildingName")
    List<String> findAllBuildingNames();

    List<Product> findByBuildingNameOrderByRoomName(String buildingName);
}
