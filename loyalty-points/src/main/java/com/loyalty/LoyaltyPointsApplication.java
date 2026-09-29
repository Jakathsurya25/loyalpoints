package com.loyalty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LoyaltyPointsApplication {
    public static void main(String[] args) {
        SpringApplication.run(LoyaltyPointsApplication.class, args);
    }
}
