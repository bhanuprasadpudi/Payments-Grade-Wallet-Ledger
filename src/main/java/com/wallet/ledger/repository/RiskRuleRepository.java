package com.wallet.ledger.repository;

import com.wallet.ledger.domain.RiskRule;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RiskRuleRepository extends JpaRepository<RiskRule, Long> {

    List<RiskRule> findByEnabledTrue();
}
