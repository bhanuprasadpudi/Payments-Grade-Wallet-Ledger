package com.wallet.ledger;

import static org.mockito.Mockito.*;

import com.wallet.ledger.domain.JobLock;
import com.wallet.ledger.repository.*;
import com.wallet.ledger.service.ReconciliationService;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ReconciliationServiceTests {

    @Test
    void cleanBooksProduceNoAnomalies() {
        JobLockRepository locks = mock(JobLockRepository.class);
        ReconciliationQueryRepository queries = mock(ReconciliationQueryRepository.class);
        WalletRepository wallets = mock(WalletRepository.class);
        ReconciliationAnomalyRepository anomalies = mock(ReconciliationAnomalyRepository.class);
        ReconciliationRunRepository runs = mock(ReconciliationRunRepository.class);
        when(locks.tryLockNowait("nightly-reconciliation")).thenReturn(Optional.of(new JobLock("nightly-reconciliation")));
        when(wallets.sumUserBalances()).thenReturn(0L);

        new ReconciliationService(locks, queries, wallets, anomalies, runs).run();

        verify(anomalies, never()).save(any());
    }
}
