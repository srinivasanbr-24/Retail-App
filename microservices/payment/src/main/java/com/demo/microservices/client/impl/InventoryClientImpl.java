package com.demo.microservices.client.impl;

import com.demo.microservices.client.InventoryClient;
import com.demo.microservices.dto.DeductErrorResponse;
import com.demo.microservices.dto.DeductRequest;
import com.demo.microservices.exception.InventoryDeductionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryClientImpl implements InventoryClient {

    private final WebClient inventoryWebClient;

    private static final String DEDUCT_STOCK_PATH = "/stock/deduct";


    @Override
    public void deductStock(List<DeductRequest> requests) throws InventoryDeductionException {

        log.info("Attempting to call Inventory Service: POST {}", DEDUCT_STOCK_PATH);
        try {
            inventoryWebClient.post()
                    .uri(DEDUCT_STOCK_PATH)
                    .bodyValue(requests) // Send the List<DeductRequest> as the JSON body
                    .retrieve()

                    .onStatus(status -> status == HttpStatus.CONFLICT, response ->
                            response.bodyToMono(DeductErrorResponse.class)
                                    .flatMap(errorBody -> {
                                        log.warn("Inventory Service returned 409 Conflict ({}). SKU: {}",
                                                errorBody.getError(), errorBody.getSku());

                                        throw new InventoryDeductionException(
                                                errorBody.getError(),
                                                errorBody.getSku()
                                        );
                                    })
                    ).onStatus(status -> status.isError(), response -> {
                        log.error("Inventory Service returned non-409 error status: {}", response.statusCode());
                        return response.createException();
                    })
                    .bodyToMono(Void.class)
                    .block();
            log.info("Inventory deduction confirmed by Inventory Service.");
        } catch (InventoryDeductionException e) {
            throw e;
        } catch (WebClientResponseException e) {
            log.error("Generic WebClient error during inventory deduction: {}", e.getMessage());
            throw new RuntimeException("Failed to communicate with Inventory Service.", e);
        } catch (Exception e) {
            log.error("Connection error during inventory deduction: {}", e.getMessage());
            throw new RuntimeException("Connection error to Inventory Service.", e);
        }

    }
}
