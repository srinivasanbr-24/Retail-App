package com.demo.microservices.service;

import com.demo.microservices.dto.DeductRequest;
import com.demo.microservices.dto.DeductSuccess;
import com.demo.microservices.exception.InsufficientStockException;
import com.demo.microservices.model.Product;

import java.util.List;


public interface InventoryService {

    List<Product> getAllProducts();

    Product getProductBySku(String sku);

    DeductSuccess deductStock(List<DeductRequest> request) throws InsufficientStockException;

     void addProducts(List<Product> products);
}
