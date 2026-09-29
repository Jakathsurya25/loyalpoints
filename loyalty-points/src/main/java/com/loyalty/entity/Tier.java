package com.loyalty.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class Tier {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String name;

    @Column(nullable = false, unique = true)
    private Long minPoints;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercent;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getMinPoints() { return minPoints; }
    public void setMinPoints(Long minPoints) { this.minPoints = minPoints; }

    public BigDecimal getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(BigDecimal discountPercent) { this.discountPercent = discountPercent; }
}
