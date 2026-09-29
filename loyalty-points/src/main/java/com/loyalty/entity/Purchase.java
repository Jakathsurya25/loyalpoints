package com.loyalty.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Purchase {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    /** Snapshot of the tier discount applied, so history stays correct if tiers change later. */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal tierDiscountPercent;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal tierDiscountAmount;

    @Column(nullable = false)
    private long pointsRedeemed;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal redemptionDiscount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal finalAmount;

    @Column(nullable = false)
    private long pointsEarned;

    @Column(nullable = false, updatable = false)
    private LocalDateTime purchaseDate;

    @PrePersist
    void onCreate() { this.purchaseDate = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getTierDiscountPercent() { return tierDiscountPercent; }
    public void setTierDiscountPercent(BigDecimal tierDiscountPercent) { this.tierDiscountPercent = tierDiscountPercent; }

    public BigDecimal getTierDiscountAmount() { return tierDiscountAmount; }
    public void setTierDiscountAmount(BigDecimal tierDiscountAmount) { this.tierDiscountAmount = tierDiscountAmount; }

    public long getPointsRedeemed() { return pointsRedeemed; }
    public void setPointsRedeemed(long pointsRedeemed) { this.pointsRedeemed = pointsRedeemed; }

    public BigDecimal getRedemptionDiscount() { return redemptionDiscount; }
    public void setRedemptionDiscount(BigDecimal redemptionDiscount) { this.redemptionDiscount = redemptionDiscount; }

    public BigDecimal getFinalAmount() { return finalAmount; }
    public void setFinalAmount(BigDecimal finalAmount) { this.finalAmount = finalAmount; }

    public long getPointsEarned() { return pointsEarned; }
    public void setPointsEarned(long pointsEarned) { this.pointsEarned = pointsEarned; }

    public LocalDateTime getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDateTime purchaseDate) { this.purchaseDate = purchaseDate; }
}
