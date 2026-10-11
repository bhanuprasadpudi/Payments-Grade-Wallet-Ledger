package com.wallet.ledger.repository;

import com.wallet.ledger.domain.LedgerEntry;
import com.wallet.ledger.domain.LedgerEntryType;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {

    @Query("""
            select coalesce(sum(e.amount), 0) from LedgerEntry e
            where e.entryType = :entryType
            """)
    long sumByType(@Param("entryType") LedgerEntryType entryType);

    long countByTransferId(UUID transferId);
}
