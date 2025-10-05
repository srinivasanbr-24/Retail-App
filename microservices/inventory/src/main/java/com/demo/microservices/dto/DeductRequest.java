package com.demo.microservices.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DeductRequest {
    private String sku;
    private int deductQuantity;
}
