package com.wallet.ledger.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reconciliation_anomalies")
public class ReconciliationAnomaly {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "run_id", nullable = false)
    private UUID runId;

    @Column(name = "anomaly_code", nullable = false, length = 100)
    private String anomalyCode;

    @Column(nullable = false, length = 2000)
    private String details;

    @Column(name = "detected_at", nullable = false, updatable = false)
    private Instant detectedAt;

    protected ReconciliationAnomaly() {
    }

    public ReconciliationAnomaly(UUID runId, String anomalyCode, String details) {
        this.runId = runId;
        this.anomalyCode = anomalyCode;
        this.details = details;
        this.detectedAt = Instant.now();
    }
}
