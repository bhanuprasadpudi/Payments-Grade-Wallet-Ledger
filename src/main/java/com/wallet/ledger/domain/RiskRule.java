package com.wallet.ledger.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "risk_rules")
public class RiskRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_code", nullable = false, unique = true, length = 100)
    private String ruleCode;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "threshold_amount")
    private Long thresholdAmount;

    @Column(name = "threshold_count")
    private Integer thresholdCount;

    @Column(name = "window_seconds")
    private Integer windowSeconds;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected RiskRule() {
    }

    public RiskRule(String ruleCode, String description, Long thresholdAmount,
                    Integer thresholdCount, Integer windowSeconds) {
        this.ruleCode = ruleCode;
        this.description = description;
        this.thresholdAmount = thresholdAmount;
        this.thresholdCount = thresholdCount;
        this.windowSeconds = windowSeconds;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public Long getId() { return id; }
    public String getRuleCode() { return ruleCode; }
    public String getDescription() { return description; }
    public Long getThresholdAmount() { return thresholdAmount; }
    public Integer getThresholdCount() { return thresholdCount; }
    public Integer getWindowSeconds() { return windowSeconds; }
    public boolean isEnabled() { return enabled; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
