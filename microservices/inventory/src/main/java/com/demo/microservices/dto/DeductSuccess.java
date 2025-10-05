package com.demo.microservices.dto;

import lombok.Data;

@Data
public class DeductSuccess {
    private String status;

    public DeductSuccess(String status) {
        this.status = status;
    }
}
