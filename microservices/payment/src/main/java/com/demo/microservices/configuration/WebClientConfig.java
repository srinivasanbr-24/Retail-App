package com.demo.microservices.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    private static final String INVENTORY_SERVICE_BASE_URL = "http://localhost:8083/api/v1/inventory";

    @Bean
    public WebClient inventoryWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(INVENTORY_SERVICE_BASE_URL)
                .build();
    }
}
