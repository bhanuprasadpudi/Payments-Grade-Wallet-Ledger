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
@Table(name = "wallets")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "user_id")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private AccountType accountType;

    @Column(nullable = false, length = 3)
    private String currency = "INR";

    @Column(nullable = false)
    private long balance;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Wallet() {
    }

    public Wallet(UUID userId, AccountType accountType, String currency, long balance) {
        this.userId = userId;
        this.accountType = accountType;
        this.currency = currency;
        this.balance = balance;
        this.createdAt = Instant.now();
    }

    public Long getAccountId() { return accountId; }
    public UUID getUserId() { return userId; }
    public AccountType getAccountType() { return accountType; }
    public String getCurrency() { return currency; }
    public long getBalance() { return balance; }
}
