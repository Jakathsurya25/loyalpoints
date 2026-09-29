package com.loyalty.dto;

import java.math.BigDecimal;

public record CustomerResponse(
        Long id, String name, String email, String phone,
        String tier, BigDecimal tierDiscountPercent,
        long lifetimePoints, long availablePoints,
        String nextTier, Long pointsToNextTier) {}
