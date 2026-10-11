package com.wallet.ledger.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReconciliationQueryRepository extends JpaRepository<com.wallet.ledger.domain.Wallet, Long> {

    @Query(value = """
            SELECT COUNT(*) FROM transfers t
            LEFT JOIN ledger_entries e ON e.transfer_id = t.id
            WHERE e.id IS NULL
            """, nativeQuery = true)
    long countTransfersWithoutLedgerEntries();

    @Query(value = """
            SELECT COUNT(*) FROM ledger_entries e
            LEFT JOIN transfers t ON t.id = e.transfer_id
            WHERE t.id IS NULL
            """, nativeQuery = true)
    long countOrphanLedgerEntries();

    @Query(value = """
            SELECT COALESCE(SUM(amount), 0) FROM ledger_entries
            WHERE entry_type = 'DEBIT'
            """, nativeQuery = true)
    long sumDebits();

    @Query(value = """
            SELECT COALESCE(SUM(amount), 0) FROM ledger_entries
            WHERE entry_type = 'CREDIT'
            """, nativeQuery = true)
    long sumCredits();

    @Query(value = """
            SELECT COUNT(*) FROM wallets
            WHERE balance < 0
            """, nativeQuery = true)
    long countNegativeWallets();

    @Query(value = """
            SELECT COUNT(*) FROM (
              SELECT w.account_id
              FROM wallets w
              LEFT JOIN ledger_entries e ON e.account_id = w.account_id
              GROUP BY w.account_id, w.balance
              HAVING w.balance <> COALESCE(SUM(
                CASE WHEN e.entry_type = 'CREDIT' THEN e.amount ELSE -e.amount END
              ), 0)
            ) mismatches
            """, nativeQuery = true)
    long countStoredBalanceMismatches();

    @Query(value = """
            SELECT COUNT(*) FROM (
              SELECT t.id
              FROM transfers t
              LEFT JOIN ledger_entries e ON e.transfer_id = t.id
              GROUP BY t.id
              HAVING COALESCE(SUM(CASE WHEN e.entry_type = 'DEBIT' THEN e.amount ELSE 0 END), 0)
                  <> COALESCE(SUM(CASE WHEN e.entry_type = 'CREDIT' THEN e.amount ELSE 0 END), 0)
            ) mismatches
            """, nativeQuery = true)
    long countUnbalancedTransfers();
}
