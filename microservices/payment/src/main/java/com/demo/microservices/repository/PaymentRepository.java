package com.demo.microservices.repository;

import com.demo.microservices.model.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends MongoRepository<Transaction, String> {
}
