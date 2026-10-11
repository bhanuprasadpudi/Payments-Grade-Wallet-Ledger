package com.wallet.ledger.repository;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StatementRepository extends JpaRepository<com.wallet.ledger.domain.Transfer, UUID> {

    @Query(value = """
            SELECT t.id FROM transfers t
            JOIN wallets w ON w.account_id = t.source_account_id
            WHERE w.user_id = :userId AND t.created_at >= :since
            ORDER BY t.created_at DESC, t.id DESC
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    java.util.List<UUID> outgoingIds(@Param("userId") UUID userId, @Param("since") Instant since,
                                     @Param("limit") int limit, @Param("offset") int offset);

    @Query(value = """
            SELECT COUNT(*) FROM transfers t
            JOIN wallets w ON w.account_id = t.source_account_id
            WHERE w.user_id = :userId AND t.created_at >= :since
            """, nativeQuery = true)
    long countOutgoingSince(@Param("userId") UUID userId, @Param("since") Instant since);

    @Query(value = """
            SELECT COALESCE(SUM(t.amount), 0) FROM transfers t
            JOIN wallets w ON w.account_id = t.source_account_id
            WHERE w.user_id = :userId AND t.created_at >= :since
            """, nativeQuery = true)
    long sumOutgoingSince(@Param("userId") UUID userId, @Param("since") Instant since);

    @Query(value = """
            SELECT COUNT(*) FROM transfers t
            JOIN wallets w ON w.account_id = t.destination_account_id
            WHERE w.user_id = :userId AND t.created_at >= :since
            """, nativeQuery = true)
    long countIncomingSince(@Param("userId") UUID userId, @Param("since") Instant since);

    @Query(value = """
            SELECT COALESCE(SUM(t.amount), 0) FROM transfers t
            JOIN wallets w ON w.account_id = t.destination_account_id
            WHERE w.user_id = :userId AND t.created_at >= :since
            """, nativeQuery = true)
    long sumIncomingSince(@Param("userId") UUID userId, @Param("since") Instant since);

    @Query(value = """
            SELECT COALESCE(MAX(t.amount), 0) FROM transfers t
            JOIN wallets w ON w.account_id = t.source_account_id
            WHERE w.user_id = :userId AND t.created_at >= :since
            """, nativeQuery = true)
    long largestOutgoingSince(@Param("userId") UUID userId, @Param("since") Instant since);
}
