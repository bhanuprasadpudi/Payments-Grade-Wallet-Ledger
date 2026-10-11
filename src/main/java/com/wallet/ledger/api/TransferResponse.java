package com.wallet.ledger.api;

import com.wallet.ledger.domain.Transfer;
import java.time.Instant;
import java.util.UUID;

public record TransferResponse(UUID id, long amount, String currency, String status, Instant createdAt) {

    public static TransferResponse from(Transfer transfer) {
        return new TransferResponse(transfer.getId(), transfer.getAmount(), transfer.getCurrency(),
                transfer.getStatus().name(), transfer.getCreatedAt());
    }
}
