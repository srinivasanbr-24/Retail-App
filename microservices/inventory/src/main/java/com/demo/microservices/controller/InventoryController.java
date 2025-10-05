package com.demo.microservices.controller;

import com.demo.microservices.dto.DeductErrorResponse;
import com.demo.microservices.dto.DeductRequest;
import com.demo.microservices.dto.DeductSuccess;
import com.demo.microservices.exception.InsufficientStockException;
import com.demo.microservices.model.Product;
import com.demo.microservices.service.InventoryService;
import com.demo.microservices.view.Views;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/inventory")
@Slf4j
public class InventoryController {

    @Autowired
    InventoryService inventoryService;

    @GetMapping("/products" )
    @JsonView(Views.Basic.class)
    public ResponseEntity<List<Product>> getAllProducts() {
        try{
            return ResponseEntity.ok(inventoryService.getAllProducts());
        } catch (Exception e){
            log.error("Error fetching products: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/product/{sku}")
    @JsonView(Views.Detailed.class)
    public ResponseEntity<Product> getProductBySku(@PathVariable String sku) {
        try{
            return ResponseEntity.ok(inventoryService.getProductBySku(sku));
        } catch (Exception e){
            log.error("Error fetching product by SKU {}: {}", sku, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/stock/deduct")
    public ResponseEntity<?> deductStock(@RequestBody List<DeductRequest> request) {
        try {
            DeductSuccess successResponse = inventoryService.deductStock(request);
            return ResponseEntity.ok(successResponse);
        } catch (InsufficientStockException e) {
            log.warn("Stock deduction failed due to insufficient stock: {}", e.getMessage());
            DeductErrorResponse errorResponse = e.getErrorResponse();
            return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
        /*} catch (RuntimeException e) {
            log.error("Error while deducting stock: {}", e.getMessage(), e);
            throw e; */
        }
    }

    @PostMapping("/add/stock")
    public void addStock(@RequestBody List<Product> request) {
            inventoryService.addProducts(request);
    }
}
