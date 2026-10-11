package com.wallet.ledger.service;

import com.wallet.ledger.api.StatementAnswer;
import com.wallet.ledger.api.StatementQuestion;
import com.wallet.ledger.repository.StatementRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StatementAiService {

    private static final Pattern DAYS = Pattern.compile("(\\d+)\\s*day", Pattern.CASE_INSENSITIVE);
    private final StatementRepository statementRepository;

    public StatementAiService(StatementRepository statementRepository) {
        this.statementRepository = statementRepository;
    }

    @Transactional(readOnly = true)
    public StatementAnswer answer(UUID callerId, StatementQuestion question) {
        String normalized = question.question().toLowerCase(Locale.ROOT);
        Matcher matcher = DAYS.matcher(normalized);
        long days = matcher.find() ? Math.min(Long.parseLong(matcher.group(1)), 365) : 30;
        Instant since = Instant.now().minus(days, ChronoUnit.DAYS);
        boolean received = normalized.contains("received") || normalized.contains("receive");
        boolean largest = normalized.contains("largest") || normalized.contains("biggest");
        long count = received ? statementRepository.countIncomingSince(callerId, since)
                : statementRepository.countOutgoingSince(callerId, since);
        long total = received ? statementRepository.sumIncomingSince(callerId, since)
                : statementRepository.sumOutgoingSince(callerId, since);
        if (largest) {
            total = statementRepository.largestOutgoingSince(callerId, since);
        }
        java.util.List<UUID> ids = statementRepository.outgoingIds(callerId, since, 100, 0);
        String operation = largest ? "largest" : received ? "received" : "sent";
        return new StatementAnswer("You had " + count + " " + operation + " transfer(s) in the last " + days
                + " day(s), totaling " + total + " paise.", count, total, ids);
    }
}
