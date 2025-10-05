package com.demo.microservices.exception;

import lombok.Getter;

@Getter
public class InventoryDeductionException extends RuntimeException {

    private final String reason;
    private final String failedSku;

    public InventoryDeductionException(String reason, String failedSku) {
        super(String.format("Inventory deduction failed. Reason: %s, SKU: %s", reason, failedSku));
        this.reason = reason;
        this.failedSku = failedSku;
    }
}