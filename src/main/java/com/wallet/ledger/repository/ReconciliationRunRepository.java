package com.wallet.ledger.repository;

import com.wallet.ledger.domain.ReconciliationRun;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReconciliationRunRepository extends JpaRepository<ReconciliationRun, UUID> {
}
