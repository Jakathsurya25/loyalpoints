package com.loyalty.repository;

import com.loyalty.entity.Tier;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TierRepository extends JpaRepository<Tier, Long> {
    Optional<Tier> findByName(String name);
    Optional<Tier> findByMinPoints(Long minPoints);
    List<Tier> findAllByOrderByMinPointsAsc();
    /** Entry tier for new customers. */
    Optional<Tier> findFirstByOrderByMinPointsAsc();
    /** Highest tier the given lifetime points qualify for. */
    Optional<Tier> findFirstByMinPointsLessThanEqualOrderByMinPointsDesc(Long points);
    /** Next tier above the given lifetime points. */
    Optional<Tier> findFirstByMinPointsGreaterThanOrderByMinPointsAsc(Long points);
}
