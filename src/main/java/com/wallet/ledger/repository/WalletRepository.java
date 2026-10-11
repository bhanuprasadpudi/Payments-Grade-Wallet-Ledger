package com.wallet.ledger.repository;

import com.wallet.ledger.domain.Wallet;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByUserId(UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from Wallet w where w.accountId = :accountId")
    Optional<Wallet> findByIdForUpdate(@Param("accountId") Long accountId);

    @Query(value = """
            SELECT * FROM wallets
            WHERE account_id IN (:accountIds)
            ORDER BY account_id ASC
            FOR UPDATE
            """, nativeQuery = true)
    List<Wallet> lockAccountsInOrder(@Param("accountIds") List<Long> accountIds);

    @Modifying
    @Query(value = """
            UPDATE wallets
            SET balance = balance + :delta
            WHERE account_id = :accountId
            AND balance + :delta >= 0
            """, nativeQuery = true)
    int adjustBalance(@Param("accountId") Long accountId, @Param("delta") long delta);

    @Query(value = """
            SELECT COALESCE(SUM(balance), 0) FROM wallets
            WHERE account_type = 'USER'
            """, nativeQuery = true)
    long sumUserBalances();
}
