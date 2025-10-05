package com.demo.microservices.dto;

import lombok.Data;

@Data
public class ItemRequest {
    private String sku;
    private int quantity;
}
