package com.wallet.ledger.api;

import com.wallet.ledger.service.StatementAiService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/{id}/statement")
public class StatementController {

    private final StatementAiService statementAiService;

    public StatementController(StatementAiService statementAiService) {
        this.statementAiService = statementAiService;
    }

    @PostMapping("/ask")
    public StatementAnswer ask(@PathVariable UUID id, @RequestHeader("X-User-Id") UUID callerId,
                               @Valid @RequestBody StatementQuestion question) {
        if (!id.equals(callerId)) {
            throw new com.wallet.ledger.service.LedgerException("FORBIDDEN", "Caller does not own this statement");
        }
        return statementAiService.answer(callerId, question);
    }
}
