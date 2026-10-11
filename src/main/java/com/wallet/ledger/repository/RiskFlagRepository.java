package com.wallet.ledger.repository;

import com.wallet.ledger.domain.RiskFlag;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RiskFlagRepository extends JpaRepository<RiskFlag, Long> {

    long countByUserIdAndCreatedAtGreaterThanEqual(UUID userId, Instant start);
}
