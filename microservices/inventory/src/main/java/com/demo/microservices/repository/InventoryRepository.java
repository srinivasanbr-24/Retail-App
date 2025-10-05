package com.demo.microservices.repository;

import com.demo.microservices.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends MongoRepository<Product, String> {
    Optional<Product> findBySku(String sku);
}
