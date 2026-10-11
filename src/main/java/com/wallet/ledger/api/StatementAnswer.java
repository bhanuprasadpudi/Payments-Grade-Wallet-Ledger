package com.wallet.ledger.api;

public record StatementAnswer(String answer, long matchingTransfers, long totalAmount,
                              java.util.List<java.util.UUID> transferIds) {
}
