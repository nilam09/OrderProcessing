package com.example.orderprocessing.repository;

import com.example.orderprocessing.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    java.util.Optional<Product> findByName(String name);
}