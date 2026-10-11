package com.wallet.ledger.api;

public record ApiError(String code, String message, String request_id) {
}
