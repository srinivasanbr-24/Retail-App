package com.demo.microservices.exception;


import com.demo.microservices.dto.DeductErrorResponse;
import com.demo.microservices.utility.InventoryDeductionStatus;
import lombok.Getter;

@Getter
public class InsufficientStockException extends RuntimeException {
    private final DeductErrorResponse errorResponse;

    public InsufficientStockException(String sku) {
        super("Insufficient stock for SKU: " + sku);
        this.errorResponse = new DeductErrorResponse(
                InventoryDeductionStatus.INSUFFICIENT_STOCK.getApiValue(),
                sku
        );
    }

}
