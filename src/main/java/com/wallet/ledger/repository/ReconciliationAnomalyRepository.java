package com.wallet.ledger.repository;

import com.wallet.ledger.domain.ReconciliationAnomaly;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReconciliationAnomalyRepository extends JpaRepository<ReconciliationAnomaly, Long> {

    long countByRunId(UUID runId);
}
