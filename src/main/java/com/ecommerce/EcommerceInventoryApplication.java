package com.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class EcommerceInventoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcommerceInventoryApplication.class, args);
    }
}