package com.demo.microservices.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Global Filter to log request, response details, and latency for every incoming request.
 */
@Component
public class LoggingGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(LoggingGlobalFilter.class);
    private static final String START_TIME = "startTime";

    /**
     * Executes the filter logic.
     * 1. Logs request details and captures start time before routing.
     * 2. Logs response details and calculates latency after the request returns.
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // --- PRE-ROUTING LOGGING ---

        // Store the start time in the exchange attributes
        exchange.getAttributes().put(START_TIME, Instant.now());

        log.info("GATEWAY REQUEST | ID: {} | Method: {} | URI: {} | Host: {}",
                request.getId(),
                request.getMethod(),
                request.getURI(),
                request.getRemoteAddress());

        // Continue the filter chain (route to downstream service)
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            // --- POST-ROUTING LOGGING ---

            ServerHttpResponse response = exchange.getResponse();
            Instant endTime = Instant.now();
            Instant startTime = exchange.getAttribute(START_TIME);

            long latency = 0;
            if (startTime != null) {
                latency = endTime.toEpochMilli() - startTime.toEpochMilli();
            }

            log.info("GATEWAY RESPONSE | ID: {} | Status: {} | Latency: {}ms | Service: {}",
                    request.getId(),
                    response.getStatusCode(),
                    latency,
                    // We can try to infer the service URI/ID if we were using the discovery locator,
                    // but for static routing, we log the destination URI for context.
                    exchange.getAttributeOrDefault("uri", "N/A"));
        }));
    }

    /**
     * Sets the order for the filter. Ordered.HIGHEST_PRECEDENCE ensures it runs first.
     */
    @Override
    public int getOrder() {
        // Run this filter very early to ensure it wraps the entire request lifecycle
        return Ordered.HIGHEST_PRECEDENCE;
    }
}

