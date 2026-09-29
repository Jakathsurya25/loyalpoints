package com.loyalty.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record PurchaseRequest(
        @NotNull(message = "customerId is required") Long customerId,
        @NotNull(message = "amount is required") @Positive(message = "amount must be > 0") BigDecimal amount,
        @PositiveOrZero(message = "pointsToRedeem cannot be negative") Long pointsToRedeem) {}
