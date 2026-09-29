package com.loyalty.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record TierRequest(
        @NotBlank(message = "name is required") @Size(max = 30) String name,
        @NotNull(message = "minPoints is required") @PositiveOrZero Long minPoints,
        @NotNull(message = "discountPercent is required")
        @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal discountPercent) {}
