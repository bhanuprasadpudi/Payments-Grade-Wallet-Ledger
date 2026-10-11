CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO users (id, name, email, phone, kyc_status)
SELECT gen_random_uuid(), 'Seed User ' || n, 'seed-' || n || '@example.test',
       '+919000' || lpad(n::text, 6, '0'), 'VERIFIED'
FROM generate_series(1, 1000) AS n
ON CONFLICT (email) DO NOTHING;

INSERT INTO wallets (user_id, account_type, currency, balance)
SELECT u.id, 'USER', 'INR', 0
FROM users u
WHERE u.email LIKE 'seed-%@example.test'
  AND NOT EXISTS (SELECT 1 FROM wallets w WHERE w.user_id = u.id);

INSERT INTO wallets (user_id, account_type, currency, balance)
SELECT u.id, 'USER', 'INR', 0
FROM users u
WHERE u.email LIKE 'seed-%@example.test'
  AND (SELECT COUNT(*) FROM wallets w WHERE w.user_id = u.id) < 2;

INSERT INTO wallets (user_id, account_type, currency, balance)
VALUES (NULL, 'CASH_IN', 'INR', 10000000000),
       (NULL, 'CASH_OUT', 'INR', 0),
       (NULL, 'FEE_REVENUE', 'INR', 0)
ON CONFLICT (account_type) DO NOTHING;

DO $$
DECLARE
    source_id BIGINT;
    destination_id BIGINT;
    transfer_id UUID;
    amount BIGINT;
BEGIN
    FOR n IN 1..100000 LOOP
        SELECT account_id INTO source_id
        FROM wallets
        WHERE account_type = 'USER'
        ORDER BY account_id
        OFFSET ((n - 1) % 1000) LIMIT 1;
        SELECT account_id INTO destination_id
        FROM wallets
        WHERE account_type = 'USER'
        ORDER BY account_id
        OFFSET (n % 1000) LIMIT 1;
        amount := (n % 100) + 1;
        transfer_id := gen_random_uuid();
        INSERT INTO transfers (id, source_account_id, destination_account_id,
                               amount, currency, status, transfer_type, note)
        VALUES (transfer_id, source_id, destination_id, amount, 'INR',
                'COMPLETED', 'PEER', 'seed data');
        INSERT INTO ledger_entries (transfer_id, account_id, entry_type,
                                    amount, balance_after)
        VALUES (transfer_id, source_id, 'DEBIT', amount, 0),
               (transfer_id, destination_id, 'CREDIT', amount, 0);
    END LOOP;
END $$;
