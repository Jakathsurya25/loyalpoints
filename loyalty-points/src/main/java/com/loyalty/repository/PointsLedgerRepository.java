package com.loyalty.repository;

import com.loyalty.entity.PointsLedger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointsLedgerRepository extends JpaRepository<PointsLedger, Long> {
    Page<PointsLedger> findByCustomerId(Long customerId, Pageable pageable);
}
