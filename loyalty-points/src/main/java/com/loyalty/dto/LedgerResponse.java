package com.loyalty.dto;

import com.loyalty.entity.LedgerType;
import java.time.LocalDateTime;

public record LedgerResponse(
        Long id, LedgerType type, long points, long balanceAfter,
        Long purchaseId, String description, LocalDateTime createdAt) {}
