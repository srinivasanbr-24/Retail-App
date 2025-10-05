package com.demo.microservices.filter;

import com.demo.microservices.config.IpFilterConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * Global filter to restrict access based on the client's IP address.
 * Only IPs listed in IpFilterConfig are allowed to proceed to downstream services.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AllowedIpFilter implements GlobalFilter, Ordered {

    private final IpFilterConfig config;
    private static final String CANONICAL_IPV6_LOOPBACK = "0:0:0:0:0:0:0:1";
    private static final String SHORTHAND_IPV6_LOOPBACK = "::1";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        String clientIp = extractAndNormalizeIp(request);

        if (clientIp == null) {
            // Block requests where IP cannot be determined for security
            log.warn("Blocking request due to null IP address.");
            return this.rejectRequest(exchange, "IP_UNKNOWN");
        }

        // --- 2. Validate the IP ---
        // Note: The normalization in extractAndNormalizeIp should ensure clientIp is in the expected format (127.0.0.1 or ::1)
        List<String> allowedIps = config.getAllowedIps();
        if (allowedIps == null || !allowedIps.contains(clientIp)) {
            log.warn("Blocking unauthorized access from IP: {}", clientIp);
            return this.rejectRequest(exchange, "IP_NOT_AUTHORIZED");
        }

        // --- 3. Allow Access ---
        log.debug("Access granted for IP: {}", clientIp);
        return chain.filter(exchange);
    }

    /**
     * Extracts the client IP from the request (handling XFF header), 
     * and normalizes IPv6 addresses to their shorthand form (e.g., ::1).
     */
    private String extractAndNormalizeIp(ServerHttpRequest request) {
        String clientIp = null;

        // 1. Check X-Forwarded-For header
        String forwardedIp = request.getHeaders().getFirst("X-Forwarded-For");
        if (forwardedIp != null) {
            clientIp = forwardedIp.split(",")[0].trim();
        }

        // 2. Fallback to remote address
        if (clientIp == null && request.getRemoteAddress() != null) {
            clientIp = request.getRemoteAddress().getAddress().getHostAddress();
        }

        if (clientIp == null) {
            return null;
        }

        // --- 3. Critical Normalization Step ---
        // A. Explicitly handle the canonical IPv6 loopback format
        if (CANONICAL_IPV6_LOOPBACK.equals(clientIp)) {
            return SHORTHAND_IPV6_LOOPBACK; // returns "::1"
        }

        // B. General Normalization: Tries to normalize other IPv6 addresses (e.g., 2001:db8::1)
        try {
            // InetAddress.getHostAddress() often returns the shorthand (::1) for loopback, 
            // but the explicit check above is safer for 0:0:0:0:0:0:0:1.
            return InetAddress.getByName(clientIp).getHostAddress();
        } catch (UnknownHostException e) {
            // If parsing fails (e.g., it's a malformed IP), return the original IP string
            return clientIp;
        }
    }

    private Mono<Void> rejectRequest(ServerWebExchange exchange, String reason) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -100; // High priority: runs very early
    }
}
