package com.demo.microservices.dto;

import lombok.Data;

@Data
public class DeductErrorResponse {
    private String error;
    private String sku;

    public DeductErrorResponse(String error, String sku) {
        this.error = error;
        this.sku = sku;
    }

}
