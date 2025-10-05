package com.demo.microservices.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DeductRequest {
    private String sku;
    private int deductQuantity;
}
