package com.wallet.ledger.api;

import com.wallet.ledger.domain.Wallet;
import com.wallet.ledger.repository.WalletRepository;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/{userId}/wallets")
public class WalletController {
    private final WalletRepository walletRepository;
    public WalletController(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @GetMapping("/{accountId}/balance")
    public java.util.Map<String, Object> balance(@PathVariable UUID userId, @PathVariable Long accountId,
                                                  @RequestHeader("X-User-Id") UUID callerId) {
        if (!userId.equals(callerId)) {
            throw new com.wallet.ledger.service.LedgerException("FORBIDDEN", "Caller does not own this wallet");
        }
        Wallet wallet = walletRepository.findById(accountId)
                .orElseThrow(() -> new com.wallet.ledger.service.LedgerException("ACCOUNT_NOT_FOUND", "Wallet does not exist"));
        if (!userId.equals(wallet.getUserId())) {
            throw new com.wallet.ledger.service.LedgerException("FORBIDDEN", "Caller does not own this wallet");
        }
        return java.util.Map.of("account_id", accountId, "balance", wallet.getBalance(),
                "currency", wallet.getCurrency());
    }
}
