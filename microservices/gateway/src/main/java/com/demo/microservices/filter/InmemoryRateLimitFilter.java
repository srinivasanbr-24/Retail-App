package com.demo.microservices.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Custom Global Filter to enforce a simple IP-based rate limit
 * using an in-memory map (for local/testing environments without Redis).
 * * NOTE: This is NOT suitable for production or clustered environments.
 */
@Component
public class InmemoryRateLimitFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(InmemoryRateLimitFilter.class);

    // In-memory map to store the last request time for each IP
    private final ConcurrentMap<String, Instant> lastRequestTime = new ConcurrentHashMap<>();

    // Rate limit configuration loaded from application.properties/yml
    @Value("${gateway.rate-limit.requests-per-second:2}")
    private int requestsPerSecond;

    // Minimum delay between requests per client IP, calculated from the rate limit
    private Duration minDelay;

    public InmemoryRateLimitFilter(@Value("${gateway.rate-limit.requests-per-second:2}") int requestsPerSecond) {
        this.requestsPerSecond = requestsPerSecond;
        // Calculate the minimum delay allowed between requests for one IP address
        // Example: 2 requests/second -> min delay is 500 milliseconds (1000ms / 2)
        this.minDelay = Duration.ofMillis(1000L / requestsPerSecond);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
        if (remoteAddress == null) {
            // Cannot determine client IP, allow request
            return chain.filter(exchange);
        }

        String clientIp = remoteAddress.getAddress().getHostAddress();
        Instant now = Instant.now();
        Instant lastTime = lastRequestTime.get(clientIp);

        // Check if rate limit is exceeded
        if (lastTime != null && now.isBefore(lastTime.plus(minDelay))) {
            log.warn("Rate limit exceeded for IP: {}. Denying request.", clientIp);

            // Set HTTP 429 Too Many Requests status
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            return exchange.getResponse().setComplete(); // Complete the response early
        }

        // Update the last request time for this IP
        lastRequestTime.put(clientIp, now);

        // Continue the filter chain
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        // Run this filter before routing and other main business logic filters
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
