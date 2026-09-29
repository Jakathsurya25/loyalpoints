package com.loyalty.repository;

import com.loyalty.entity.Purchase;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    List<Purchase> findByCustomerIdOrderByPurchaseDateDesc(Long customerId);
}
