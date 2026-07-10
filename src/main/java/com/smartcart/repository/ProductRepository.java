package com.smartcart.repository;

import com.smartcart.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);
    List<Product> findByCategoryId(Long categoryId);
    List<Product> findByFeaturedTrue();
    List<Product> findByTrendingTrue();
}
