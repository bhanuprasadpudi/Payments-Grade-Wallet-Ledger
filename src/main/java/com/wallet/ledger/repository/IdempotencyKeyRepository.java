package com.wallet.ledger.repository;

import com.wallet.ledger.domain.IdempotencyKey;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, Long> {

    Optional<IdempotencyKey> findByUserIdAndKey(UUID userId, UUID key);

    @Modifying
    @Query("update IdempotencyKey i set i.responseStatus = :status, i.responseBody = :body where i.id = :id")
    int saveResponse(@Param("id") Long id, @Param("status") int status, @Param("body") String body);
}
