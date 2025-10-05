package com.demo.microservices;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.demo.microservices"})
public class GatewayManagementServiceApplication {
    public static void main(String[] args) {
         SpringApplication.run(GatewayManagementServiceApplication.class, args);
        }
}