CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE wallets (
    account_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id UUID REFERENCES users (id),
    account_type VARCHAR(20) NOT NULL,
    currency CHAR(3) NOT NULL DEFAULT 'INR',
    balance BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT wallets_account_type_check
        CHECK (account_type IN ('USER', 'CASH_IN', 'CASH_OUT', 'FEE_REVENUE')),
    CONSTRAINT wallets_balance_check CHECK (balance >= 0),
    CONSTRAINT wallets_user_account_check CHECK (
        (account_type = 'USER' AND user_id IS NOT NULL)
        OR (account_type <> 'USER' AND user_id IS NULL)
    ),
    CONSTRAINT wallets_user_id_unique UNIQUE (user_id),
    CONSTRAINT wallets_system_account_type_unique UNIQUE (account_type)
);

CREATE INDEX wallets_user_id_idx ON wallets (user_id);

CREATE TABLE transfers (
    id UUID PRIMARY KEY,
    source_account_id BIGINT NOT NULL REFERENCES wallets (account_id),
    destination_account_id BIGINT NOT NULL REFERENCES wallets (account_id),
    amount BIGINT NOT NULL,
    currency CHAR(3) NOT NULL DEFAULT 'INR',
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    note VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT transfers_amount_check CHECK (amount > 0),
    CONSTRAINT transfers_different_accounts_check
        CHECK (source_account_id <> destination_account_id),
    CONSTRAINT transfers_status_check
        CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'REVERSED'))
);

CREATE INDEX transfers_source_created_idx
    ON transfers (source_account_id, created_at DESC, id DESC);
CREATE INDEX transfers_destination_created_idx
    ON transfers (destination_account_id, created_at DESC, id DESC);
CREATE INDEX transfers_created_idx ON transfers (created_at DESC, id DESC);

CREATE TABLE ledger_entries (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    transfer_id UUID NOT NULL REFERENCES transfers (id),
    account_id BIGINT NOT NULL REFERENCES wallets (account_id),
    entry_type VARCHAR(10) NOT NULL,
    amount BIGINT NOT NULL,
    balance_after BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ledger_entries_entry_type_check
        CHECK (entry_type IN ('DEBIT', 'CREDIT')),
    CONSTRAINT ledger_entries_amount_check CHECK (amount > 0),
    CONSTRAINT ledger_entries_balance_after_check CHECK (balance_after >= 0),
    CONSTRAINT ledger_entries_transfer_account_type_unique
        UNIQUE (transfer_id, account_id, entry_type)
);

CREATE INDEX ledger_entries_account_created_idx
    ON ledger_entries (account_id, created_at DESC, id DESC);
CREATE INDEX ledger_entries_transfer_idx ON ledger_entries (transfer_id);

CREATE TABLE idempotency_keys (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id),
    idempotency_key UUID NOT NULL,
    request_hash VARCHAR(64) NOT NULL,
    response_status INTEGER,
    response_body TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT idempotency_keys_user_key_unique UNIQUE (user_id, idempotency_key),
    CONSTRAINT idempotency_keys_hash_check
        CHECK (request_hash ~ '^[0-9a-f]{64}$'),
    CONSTRAINT idempotency_keys_response_pair_check
        CHECK ((response_status IS NULL AND response_body IS NULL)
            OR (response_status IS NOT NULL AND response_body IS NOT NULL))
);

CREATE INDEX idempotency_keys_created_idx
    ON idempotency_keys (created_at DESC, id DESC);

CREATE TABLE risk_rules (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    rule_code VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500) NOT NULL,
    threshold_amount BIGINT,
    threshold_count INTEGER,
    window_seconds INTEGER,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT risk_rules_threshold_amount_check
        CHECK (threshold_amount IS NULL OR threshold_amount > 0),
    CONSTRAINT risk_rules_threshold_count_check
        CHECK (threshold_count IS NULL OR threshold_count > 0),
    CONSTRAINT risk_rules_window_seconds_check
        CHECK (window_seconds IS NULL OR window_seconds > 0)
);

CREATE TABLE risk_flags (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id),
    transfer_id UUID REFERENCES transfers (id),
    rule_id BIGINT NOT NULL REFERENCES risk_rules (id),
    reason VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMPTZ,
    CONSTRAINT risk_flags_status_check
        CHECK (status IN ('OPEN', 'REVIEWED', 'DISMISSED', 'CONFIRMED')),
    CONSTRAINT risk_flags_resolution_check
        CHECK ((status = 'OPEN' AND resolved_at IS NULL)
            OR (status <> 'OPEN' AND resolved_at IS NOT NULL))
);

CREATE INDEX risk_flags_user_created_idx
    ON risk_flags (user_id, created_at DESC, id DESC);
CREATE INDEX risk_flags_transfer_idx ON risk_flags (transfer_id);

CREATE TABLE job_locks (
    lock_name VARCHAR(100) PRIMARY KEY,
    locked_until TIMESTAMPTZ,
    locked_by VARCHAR(200),
    acquired_at TIMESTAMPTZ,
    CONSTRAINT job_locks_lock_state_check
        CHECK ((locked_until IS NULL AND locked_by IS NULL AND acquired_at IS NULL)
            OR (locked_until IS NOT NULL AND locked_by IS NOT NULL AND acquired_at IS NOT NULL))
);
