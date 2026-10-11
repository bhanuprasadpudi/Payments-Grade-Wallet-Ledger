package com.wallet.ledger.service;

import com.wallet.ledger.domain.JobLock;
import com.wallet.ledger.domain.ReconciliationAnomaly;
import com.wallet.ledger.repository.JobLockRepository;
import com.wallet.ledger.repository.ReconciliationAnomalyRepository;
import com.wallet.ledger.repository.ReconciliationQueryRepository;
import com.wallet.ledger.repository.ReconciliationRunRepository;
import com.wallet.ledger.repository.WalletRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReconciliationService {

    private static final String LOCK_NAME = "nightly-reconciliation";
    private final JobLockRepository jobLockRepository;
    private final ReconciliationQueryRepository queries;
    private final WalletRepository walletRepository;
    private final ReconciliationAnomalyRepository anomalyRepository;
    private final ReconciliationRunRepository runRepository;

    public ReconciliationService(JobLockRepository jobLockRepository, ReconciliationQueryRepository queries,
                                 WalletRepository walletRepository,
                                 ReconciliationAnomalyRepository anomalyRepository,
                                 ReconciliationRunRepository runRepository) {
        this.jobLockRepository = jobLockRepository;
        this.queries = queries;
        this.walletRepository = walletRepository;
        this.anomalyRepository = anomalyRepository;
        this.runRepository = runRepository;
    }

    @Scheduled(cron = "${wallet.reconciliation.cron:0 0 2 * * *}")
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void scheduledRun() {
        run();
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public UUID run() {
        try {
            jobLockRepository.tryLockNowait(LOCK_NAME)
                    .orElseThrow(() -> new LedgerException("RECONCILIATION_LOCK_MISSING", "Reconciliation lock is missing"));
        } catch (CannotAcquireLockException exception) {
            throw new LedgerException("RECONCILIATION_IN_PROGRESS", "Reconciliation is already running");
        }
        UUID runId = UUID.randomUUID();
        runRepository.save(new com.wallet.ledger.domain.ReconciliationRun(runId));
        if (queries.sumDebits() != queries.sumCredits()) {
            record(runId, "GLOBAL_BALANCE_MISMATCH", "Ledger debits and credits do not balance");
        }
        if (queries.countTransfersWithoutLedgerEntries() > 0) {
            record(runId, "TRANSFER_WITHOUT_LEDGER", "One or more transfers have no ledger entries");
        }
        if (queries.countUnbalancedTransfers() > 0) {
            record(runId, "UNBALANCED_TRANSFER", "One or more transfers have unequal debits and credits");
        }
        if (queries.countStoredBalanceMismatches() > 0) {
            record(runId, "STORED_BALANCE_MISMATCH", "A stored account balance differs from its ledger balance");
        }
        if (queries.countOrphanLedgerEntries() > 0) {
            record(runId, "ORPHAN_LEDGER_ENTRY", "One or more ledger entries have no transfer");
        }
        if (queries.countNegativeWallets() > 0 || walletRepository.sumUserBalances() < 0) {
            record(runId, "NEGATIVE_WALLET", "A wallet has a negative stored balance");
        }
        return runId;
    }

    private void record(UUID runId, String code, String details) {
        anomalyRepository.save(new ReconciliationAnomaly(runId, code, details));
    }
}
