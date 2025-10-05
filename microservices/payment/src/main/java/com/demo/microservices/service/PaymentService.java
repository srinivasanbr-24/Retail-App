package com.demo.microservices.service;

import com.demo.microservices.dto.PurchaseRequest;
import com.demo.microservices.model.Transaction;

public interface PaymentService {

    Transaction processPayment(PurchaseRequest request);
}
