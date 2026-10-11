ALTER TABLE users ADD COLUMN name VARCHAR(200) NOT NULL DEFAULT '';
ALTER TABLE users ADD COLUMN phone VARCHAR(30) NOT NULL DEFAULT '';
ALTER TABLE users ADD COLUMN kyc_status VARCHAR(20) NOT NULL DEFAULT 'PENDING';
ALTER TABLE users ADD CONSTRAINT users_kyc_status_check
    CHECK (kyc_status IN ('PENDING', 'VERIFIED', 'REJECTED'));

ALTER TABLE wallets DROP CONSTRAINT wallets_balance_check;
ALTER TABLE wallets ADD CONSTRAINT wallets_user_balance_check
    CHECK (account_type <> 'USER' OR balance >= 0);
ALTER TABLE wallets DROP CONSTRAINT wallets_user_id_unique;
CREATE UNIQUE INDEX wallets_user_id_name_idx ON wallets (user_id, account_id)
    WHERE user_id IS NOT NULL;

ALTER TABLE transfers ADD COLUMN transfer_type VARCHAR(20) NOT NULL DEFAULT 'PEER';
ALTER TABLE transfers ADD COLUMN original_transfer_id UUID REFERENCES transfers (id);
ALTER TABLE transfers ADD CONSTRAINT transfers_type_check
    CHECK (transfer_type IN ('TOP_UP', 'PEER', 'WITHDRAWAL', 'FEE', 'REVERSAL'));
CREATE INDEX transfers_original_idx ON transfers (original_transfer_id);

CREATE TABLE transfer_status_audits (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    transfer_id UUID NOT NULL REFERENCES transfers (id),
    old_status VARCHAR(20),
    new_status VARCHAR(20) NOT NULL,
    changed_by UUID REFERENCES users (id),
    changed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX transfer_status_audits_transfer_idx
    ON transfer_status_audits (transfer_id, changed_at DESC, id DESC);

CREATE TABLE reconciliation_runs (
    id UUID PRIMARY KEY,
    started_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    status VARCHAR(20) NOT NULL,
    anomaly_count INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT reconciliation_runs_status_check
        CHECK (status IN ('RUNNING', 'COMPLETED', 'FAILED'))
);

ALTER TABLE reconciliation_anomalies
    ADD COLUMN run_check VARCHAR(100) NOT NULL DEFAULT 'GENERAL';
ALTER TABLE reconciliation_anomalies ADD COLUMN entity_id VARCHAR(100);
ALTER TABLE reconciliation_anomalies ADD COLUMN expected_value BIGINT;
ALTER TABLE reconciliation_anomalies ADD COLUMN actual_value BIGINT;

CREATE OR REPLACE FUNCTION reject_ledger_mutation() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'ledger_entries are append-only';
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER ledger_entries_append_only
    BEFORE UPDATE OR DELETE ON ledger_entries
    FOR EACH ROW EXECUTE FUNCTION reject_ledger_mutation();
