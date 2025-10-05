package com.demo.microservices.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransactionError {
    private String error;
    private String reason;
}
