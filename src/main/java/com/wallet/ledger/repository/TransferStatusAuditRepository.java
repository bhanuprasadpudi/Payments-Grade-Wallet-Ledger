package com.wallet.ledger.repository;

import com.wallet.ledger.domain.TransferStatusAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferStatusAuditRepository extends JpaRepository<TransferStatusAudit, Long> {
}
