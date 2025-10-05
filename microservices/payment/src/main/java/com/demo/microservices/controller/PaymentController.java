package com.demo.microservices.controller;

import com.demo.microservices.dto.PurchaseRequest;
import com.demo.microservices.dto.TransactionError;
import com.demo.microservices.exception.InventoryDeductionException;
import com.demo.microservices.model.Transaction;
import com.demo.microservices.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/process")
    public ResponseEntity<?> processPayment(@RequestBody PurchaseRequest request) {
        try{
            Transaction transaction = paymentService.processPayment(request);
            log.info("Transaction {} completed successfully.", transaction.getTransactionId());

            return new ResponseEntity<>(transaction, HttpStatus.CREATED);
        } catch (InventoryDeductionException e) {
            log.warn("Purchase failed: Inventory deduction failed for SKU {}.", e.getFailedSku());
            TransactionError errorResponse = new TransactionError("TRANSACTION_FAILED", e.getReason());

            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Unexpected error during purchase process: {}", e.getMessage(), e);
            TransactionError errorResponse = new TransactionError(
                    "TRANSACTION_FAILED",
                    "INTERNAL_SERVICE_ERROR"
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
