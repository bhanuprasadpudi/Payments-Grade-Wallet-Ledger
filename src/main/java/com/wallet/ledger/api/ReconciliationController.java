package com.wallet.ledger.api;

import com.wallet.ledger.service.ReconciliationService;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/reconciliation")
public class ReconciliationController {

    private final ReconciliationService reconciliationService;

    public ReconciliationController(ReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    @PostMapping("/run")
    public ResponseEntity<?> run() {
        UUID runId = reconciliationService.run();
        return ResponseEntity.ok(java.util.Map.of("run_id", runId));
    }
}
