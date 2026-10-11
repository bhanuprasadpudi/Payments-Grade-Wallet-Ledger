package com.wallet.ledger.repository;

import com.wallet.ledger.domain.Transfer;
import com.wallet.ledger.domain.TransferStatus;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransferRepository extends JpaRepository<Transfer, UUID> {

    long countByCreatedAtGreaterThanEqual(Instant start);

    @Query("""
            select coalesce(sum(t.amount), 0) from Transfer t
            where t.status = :status and t.createdAt >= :start
            """)
    long sumAmountByStatusSince(@Param("status") TransferStatus status,
                                @Param("start") Instant start);
}
