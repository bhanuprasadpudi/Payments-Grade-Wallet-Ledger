package com.wallet.ledger.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reconciliation_runs")
public class ReconciliationRun {
    @Id
    private UUID id;
    @Column(name = "started_at", nullable = false)
    private Instant startedAt;
    @Column(name = "completed_at")
    private Instant completedAt;
    @Column(nullable = false, length = 20)
    private String status;
    @Column(name = "anomaly_count", nullable = false)
    private int anomalyCount;
    protected ReconciliationRun() {}
    public ReconciliationRun(UUID id) {
        this.id = id;
        this.startedAt = Instant.now();
        this.status = "RUNNING";
    }
}
