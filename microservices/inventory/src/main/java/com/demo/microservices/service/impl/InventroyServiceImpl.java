package com.demo.microservices.service.impl;

import com.demo.microservices.dto.DeductErrorResponse;
import com.demo.microservices.dto.DeductRequest;
import com.demo.microservices.dto.DeductSuccess;
import com.demo.microservices.exception.InsufficientStockException;
import com.demo.microservices.model.Product;
import com.demo.microservices.repository.InventoryRepository;
import com.demo.microservices.service.InventoryService;
import com.demo.microservices.utility.InventoryDeductionStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class InventroyServiceImpl implements InventoryService {

    @Autowired
    InventoryRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        log.info("Received request to fetch all products");
        try {
            List<Product> products = repository.findAll();
            if (products.isEmpty()) {
                log.warn("No products found in the inventory");
            } else {
                log.info("Successfully fetched products from the inventory");
            }
            log.info("Fetched {} products", products.size());
            return products;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Product getProductBySku(String sku) {
        log.info("Received request to fetch product with SKU: {}",sku);
        try{
            return repository.findBySku(sku).orElseThrow(() -> new RuntimeException("No product found with SKU: " + sku));
        } catch (Exception e) {
            log.error("Error fetching product with SKU {}: {}", sku, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional
    public  DeductSuccess deductStock(List<DeductRequest> request) {
        log.info(" Received request to deduct stock for {} products", request.size());
        try{
            for (DeductRequest r : request) {
                Product p = repository.findBySku(r.getSku()).orElseThrow(() -> new RuntimeException("No product found with SKU: " + r.getSku()));
                if (p.getQuantity() < r.getDeductQuantity()) {
                    log.warn("INSUFFICIENT_STOCK for SKU: {}", r.getSku());
                    throw new InsufficientStockException(r.getSku());
                }
                p.setQuantity(p.getQuantity() - r.getDeductQuantity());
                repository.save(p);
                log.info("Deducted {} units from SKU: {}. New stock: {}", r.getDeductQuantity(), r.getSku(), p.getQuantity());
            }
            log.info("Stock deduction completed successfully for all requested products");
            return new DeductSuccess(InventoryDeductionStatus.DEDUCTION_COMMITTED.getApiValue());
        } catch (InsufficientStockException e) {
            log.error("Error deducting stock: Insufficient stock for SKU: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Generic error deducting stock: {}", e.getMessage(), e);
            throw new RuntimeException("Service failure during stock deduction", e);
        }
    }

    @Override
    @Transactional
    public void addProducts(List<Product> products) {
        repository.saveAll(products);
    }
}
