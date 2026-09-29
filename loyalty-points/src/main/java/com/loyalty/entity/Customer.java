package com.loyalty.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Customer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(length = 20)
    private String phone;

    /** Total points ever earned. Only grows -> drives tier, so no mid-period downgrade. */
    @Column(nullable = false)
    private long lifetimePoints = 0;

    /** Spendable balance = earned - redeemed. */
    @Column(nullable = false)
    private long availablePoints = 0;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "tier_id", nullable = false)
    private Tier tier;

    @OneToMany(mappedBy = "customer")
    private List<Purchase> purchases = new ArrayList<>();

    @OneToMany(mappedBy = "customer")
    private List<PointsLedger> ledgerEntries = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() { this.createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public long getLifetimePoints() { return lifetimePoints; }
    public void setLifetimePoints(long lifetimePoints) { this.lifetimePoints = lifetimePoints; }

    public long getAvailablePoints() { return availablePoints; }
    public void setAvailablePoints(long availablePoints) { this.availablePoints = availablePoints; }

    public Tier getTier() { return tier; }
    public void setTier(Tier tier) { this.tier = tier; }

    public List<Purchase> getPurchases() { return purchases; }
    public void setPurchases(List<Purchase> purchases) { this.purchases = purchases; }

    public List<PointsLedger> getLedgerEntries() { return ledgerEntries; }
    public void setLedgerEntries(List<PointsLedger> ledgerEntries) { this.ledgerEntries = ledgerEntries; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
