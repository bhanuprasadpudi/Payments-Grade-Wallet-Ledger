package com.wallet.ledger.service;

import com.wallet.ledger.api.TransferRequest;
import com.wallet.ledger.domain.LedgerEntry;
import com.wallet.ledger.domain.LedgerEntryType;
import com.wallet.ledger.domain.Transfer;
import com.wallet.ledger.domain.TransferStatus;
import com.wallet.ledger.domain.User;
import com.wallet.ledger.domain.Wallet;
import com.wallet.ledger.repository.LedgerEntryRepository;
import com.wallet.ledger.repository.TransferRepository;
import com.wallet.ledger.repository.UserRepository;
import com.wallet.ledger.repository.WalletRepository;
import com.wallet.ledger.repository.TransferStatusAuditRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferService {

    private final WalletRepository walletRepository;
    private final TransferRepository transferRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final UserRepository userRepository;
    private final FraudService fraudService;
    private final TransferStatusAuditRepository statusAuditRepository;

    public TransferService(WalletRepository walletRepository, TransferRepository transferRepository,
                           LedgerEntryRepository ledgerEntryRepository, UserRepository userRepository,
                           FraudService fraudService, TransferStatusAuditRepository statusAuditRepository) {
        this.walletRepository = walletRepository;
        this.transferRepository = transferRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.userRepository = userRepository;
        this.fraudService = fraudService;
        this.statusAuditRepository = statusAuditRepository;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Transfer transfer(UUID callerId, TransferRequest request) {
        if (request.sourceAccountId().equals(request.destinationAccountId())) {
            throw new LedgerException("INVALID_TRANSFER", "Source and destination accounts must differ");
        }
        User caller = userRepository.findById(callerId)
                .orElseThrow(() -> new LedgerException("USER_NOT_FOUND", "Caller does not exist"));
        List<Wallet> wallets = walletRepository.lockAccountsInOrder(
                List.of(request.sourceAccountId(), request.destinationAccountId()));
        if (wallets.size() != 2) {
            throw new LedgerException("ACCOUNT_NOT_FOUND", "Both transfer accounts must exist");
        }
        Wallet source = wallets.stream().filter(w -> w.getAccountId().equals(request.sourceAccountId()))
                .findFirst().orElseThrow();
        Wallet destination = wallets.stream().filter(w -> w.getAccountId().equals(request.destinationAccountId()))
                .findFirst().orElseThrow();
        if (source.getUserId() == null || !source.getUserId().equals(caller.getId())) {
            throw new LedgerException("FORBIDDEN", "Caller does not own the source account");
        }
        if (!source.getCurrency().equals(destination.getCurrency())
                || (request.currency() != null && !source.getCurrency().equals(request.currency()))) {
            throw new LedgerException("CURRENCY_MISMATCH", "Transfer currency does not match both accounts");
        }
        if (source.getBalance() < request.amount()) {
            throw new LedgerException("INSUFFICIENT_FUNDS", "Source account has insufficient funds");
        }
        UUID transferId = UUID.randomUUID();
        Transfer transfer = new Transfer(transferId, source.getAccountId(), destination.getAccountId(),
                request.amount(), source.getCurrency(), TransferStatus.COMPLETED, request.note());
        transferRepository.save(transfer);
        statusAuditRepository.save(new com.wallet.ledger.domain.TransferStatusAudit(
                transferId, null, TransferStatus.COMPLETED, callerId));
        if (walletRepository.adjustBalance(source.getAccountId(), -request.amount()) != 1
                || walletRepository.adjustBalance(destination.getAccountId(), request.amount()) != 1) {
            throw new LedgerException("BALANCE_UPDATE_FAILED", "Could not update account balances");
        }
        ledgerEntryRepository.save(new LedgerEntry(transferId, source.getAccountId(), LedgerEntryType.DEBIT,
                request.amount(), source.getBalance() - request.amount()));
        ledgerEntryRepository.save(new LedgerEntry(transferId, destination.getAccountId(), LedgerEntryType.CREDIT,
                request.amount(), destination.getBalance() + request.amount()));
        fraudService.evaluate(callerId, transfer);
        return transfer;
    }
}
