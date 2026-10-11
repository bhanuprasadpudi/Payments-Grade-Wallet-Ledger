package com.wallet.ledger.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "risk_flags")
public class RiskFlag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "transfer_id")
    private UUID transferId;

    @Column(name = "rule_id", nullable = false)
    private Long ruleId;

    @Column(nullable = false, length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RiskFlagStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    protected RiskFlag() {
    }

    public RiskFlag(UUID userId, UUID transferId, Long ruleId, String reason) {
        this.userId = userId;
        this.transferId = transferId;
        this.ruleId = ruleId;
        this.reason = reason;
        this.status = RiskFlagStatus.OPEN;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getTransferId() { return transferId; }
    public Long getRuleId() { return ruleId; }
    public String getReason() { return reason; }
    public RiskFlagStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getResolvedAt() { return resolvedAt; }
}
