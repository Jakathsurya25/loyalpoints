package com.loyalty.config;

import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configurable rates, bound from application.properties (loyalty.*). */
@ConfigurationProperties(prefix = "loyalty")
public record LoyaltyProperties(BigDecimal pointsPerRupee, BigDecimal rupeesPerPoint) {}
