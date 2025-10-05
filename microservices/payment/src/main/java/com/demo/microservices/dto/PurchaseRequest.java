package com.demo.microservices.dto;

import lombok.Data;

import java.util.List;

@Data
public class PurchaseRequest {
    private String cardDetails;
    private List<ItemRequest> items;

}
