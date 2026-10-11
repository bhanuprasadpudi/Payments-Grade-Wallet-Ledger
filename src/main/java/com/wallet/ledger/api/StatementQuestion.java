package com.wallet.ledger.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StatementQuestion(@NotBlank @Size(max = 1000) String question) {
}
