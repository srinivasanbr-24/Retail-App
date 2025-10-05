package com.demo.microservices.client;

import com.demo.microservices.dto.DeductRequest;
import com.demo.microservices.exception.InventoryDeductionException;

import java.util.List;

public interface InventoryClient {

    void deductStock(List<DeductRequest> requests) throws InventoryDeductionException;
}
