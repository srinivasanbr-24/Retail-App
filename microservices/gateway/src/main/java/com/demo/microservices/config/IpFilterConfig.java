package com.demo.microservices.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "app.ip-filter")
@Data
public class IpFilterConfig {

    /**
     * List of trusted IP addresses that are allowed to access the gateway routes.
     * All other IPs will be blocked.
     */
    private List<String> allowedIps;
}

