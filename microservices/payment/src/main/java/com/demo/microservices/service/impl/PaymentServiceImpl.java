package com.demo.microservices.service.impl;

import com.demo.microservices.client.InventoryClient;
import com.demo.microservices.dto.DeductRequest;
import com.demo.microservices.dto.ItemRequest;
import com.demo.microservices.dto.PurchaseRequest;
import com.demo.microservices.model.Transaction;
import com.demo.microservices.repository.PaymentRepository;
import com.demo.microservices.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository repository;

    private final InventoryClient client;

    @Override
    @Transactional
    public Transaction processPayment(PurchaseRequest request) {
            List<DeductRequest> inventoryRequests = request.getItems().stream()
                    .map(item -> new DeductRequest(item.getSku(), item.getQuantity()))
                    .collect(Collectors.toList());

            client.deductStock(inventoryRequests);
            log.info("Stock successfully deducted for transaction.");

            BigDecimal totalAmount = calculateTotal(request.getItems());
            log.info("Payment captured successfully for amount: {}", totalAmount);

            Transaction transaction = Transaction.builder()
                    .transactionId("T-" + UUID.randomUUID().toString().substring(0, 8))
                    .status("COMPLETED")
                    .totalAmount(totalAmount)
                    .inventoryStatus("DEDUCTION_COMMITTED")
                    .itemsPurchased(request.getItems())
                    .timestamp(Instant.now())
                    .build();
            return repository.save(transaction);
    }

    private BigDecimal calculateTotal(List<ItemRequest> items) {
        return items.stream()
                .map(item -> BigDecimal.valueOf(19.99).multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
