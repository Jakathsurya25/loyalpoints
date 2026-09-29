package com.loyalty.dto;

import java.math.BigDecimal;

public record TierResponse(Long id, String name, Long minPoints, BigDecimal discountPercent) {}
