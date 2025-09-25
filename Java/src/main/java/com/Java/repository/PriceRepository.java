package com.Java.repository;

import com.Java.entity.Price;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PriceRepository extends JpaRepository<Price, Long> {
    List<Price> findByProductId(String productId);
    List<Price> findByCurrency(String currency);
}
