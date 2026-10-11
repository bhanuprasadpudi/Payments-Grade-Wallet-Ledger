CREATE TABLE reconciliation_anomalies (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    run_id UUID NOT NULL,
    anomaly_code VARCHAR(100) NOT NULL,
    details VARCHAR(2000) NOT NULL,
    detected_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX reconciliation_anomalies_run_idx
    ON reconciliation_anomalies (run_id, detected_at DESC, id DESC);

INSERT INTO job_locks (lock_name)
VALUES ('nightly-reconciliation')
ON CONFLICT (lock_name) DO NOTHING;
