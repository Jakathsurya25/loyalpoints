package com.loyalty.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PurchaseResponse(
        Long purchaseId, Long customerId,
        BigDecimal amount,
        BigDecimal tierDiscountPercent, BigDecimal tierDiscountAmount,
        long pointsRedeemed, BigDecimal redemptionDiscount,
        BigDecimal finalAmount, long pointsEarned,
        String tierBefore, String tierAfter, boolean tierUpgraded,
        long availablePoints, long lifetimePoints,
        LocalDateTime purchaseDate) {}
