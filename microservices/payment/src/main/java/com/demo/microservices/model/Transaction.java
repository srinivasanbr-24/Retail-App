package com.demo.microservices.model;

import com.demo.microservices.dto.ItemRequest;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@Document(collection = "transactions")
public class Transaction {
    @Id
    private String transactionId;
    private String status;
    private BigDecimal totalAmount;
    private String inventoryStatus;
    private List<ItemRequest> itemsPurchased;
    private Instant timestamp;
}
