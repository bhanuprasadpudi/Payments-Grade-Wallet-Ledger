package com.wallet.ledger.service;

import com.wallet.ledger.domain.IdempotencyKey;
import com.wallet.ledger.repository.IdempotencyKeyRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdempotencyService {

    private final IdempotencyKeyRepository repository;

    public IdempotencyService(IdempotencyKeyRepository repository) {
        this.repository = repository;
    }

    public String hash(byte[] body) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(body));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is required by the runtime", exception);
        }
    }

    @Transactional
    public IdempotencyKey reserve(UUID userId, UUID key, String requestHash) {
        return repository.findByUserIdAndKey(userId, key).orElseGet(() -> {
            try {
                return repository.saveAndFlush(new IdempotencyKey(userId, key, requestHash));
            } catch (DataIntegrityViolationException exception) {
                throw new LedgerException("IDEMPOTENCY_IN_PROGRESS",
                        "Another request with this idempotency key is in progress");
            }
        });
    }

    @Transactional
    public void complete(IdempotencyKey key, int status, String body) {
        repository.saveResponse(key.getId(), status, body);
    }

    public boolean sameBody(IdempotencyKey key, String hash) {
        return MessageDigest.isEqual(key.getRequestHash().getBytes(StandardCharsets.US_ASCII),
                hash.getBytes(StandardCharsets.US_ASCII));
    }
}
