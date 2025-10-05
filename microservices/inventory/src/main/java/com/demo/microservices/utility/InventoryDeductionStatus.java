package com.demo.microservices.utility;

public enum InventoryDeductionStatus {
    DEDUCTION_COMMITTED("DEDUCTION_COMMITTED"),
    INSUFFICIENT_STOCK("INSUFFICIENT_STOCK");

    private final String apiValue;

    InventoryDeductionStatus(String apiValue) {
        this.apiValue = apiValue;
    }

    public String getApiValue() {
        return apiValue;
    }
}
