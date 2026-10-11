package com.wallet.ledger.service;

import com.wallet.ledger.domain.RiskFlag;
import com.wallet.ledger.domain.RiskRule;
import com.wallet.ledger.domain.Transfer;
import com.wallet.ledger.repository.RiskFlagRepository;
import com.wallet.ledger.repository.RiskRuleRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FraudService {

    private final RiskRuleRepository riskRuleRepository;
    private final RiskFlagRepository riskFlagRepository;

    public FraudService(RiskRuleRepository riskRuleRepository, RiskFlagRepository riskFlagRepository) {
        this.riskRuleRepository = riskRuleRepository;
        this.riskFlagRepository = riskFlagRepository;
    }

    @Transactional
    public void evaluate(UUID userId, Transfer transfer) {
        for (RiskRule rule : riskRuleRepository.findByEnabledTrue()) {
            boolean amountExceeded = rule.getThresholdAmount() != null
                    && transfer.getAmount() >= rule.getThresholdAmount();
            boolean countExceeded = rule.getThresholdCount() != null && rule.getWindowSeconds() != null
                    && riskFlagRepository.countByUserIdAndCreatedAtGreaterThanEqual(
                    userId, Instant.now().minusSeconds(rule.getWindowSeconds())) >= rule.getThresholdCount();
            if (amountExceeded || countExceeded) {
                riskFlagRepository.save(new RiskFlag(userId, transfer.getId(), rule.getId(),
                        "Risk rule triggered: " + rule.getRuleCode()));
            }
        }
    }
}
