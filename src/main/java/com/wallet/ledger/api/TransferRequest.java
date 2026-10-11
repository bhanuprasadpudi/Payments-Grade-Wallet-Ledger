package com.wallet.ledger.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record TransferRequest(
        @NotNull Long sourceAccountId,
        @NotNull Long destinationAccountId,
        @Positive long amount,
        @Size(max = 3) String currency,
        @Size(max = 500) String note) {
}
