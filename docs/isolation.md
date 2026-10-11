# Transaction isolation

The transfer service uses PostgreSQL `READ COMMITTED` with explicit
`SELECT ... FOR UPDATE` locking of both accounts in ascending `account_id`
order. The balance is re-read while the locks are held, so concurrent spends
serialize and cannot overdraw a user wallet. `SERIALIZABLE` would detect more
write skew but would increase serialization failures and retry pressure without
improving this locked two-row money path.

Reconciliation uses `REPEATABLE READ`, ensuring every check observes one
consistent snapshot. The job first locks its `job_locks` row with
`FOR UPDATE NOWAIT`, so only one run proceeds.

For statement queries, indexes support `(account_id, created_at DESC, id DESC)`
ordering. Offset pagination becomes slower at large offsets because PostgreSQL
must walk and discard preceding rows; cursor pagination on `(created_at, id)`
is the preferred future optimization.

## Two-session lab

Run two shells against the Compose database:

```bash
docker compose up -d postgres
docker compose exec postgres psql -U wallet -d wallet
```

In both sessions, choose the same wallet:

```sql
BEGIN;
SELECT account_id, balance FROM wallets WHERE account_type = 'USER' LIMIT 1;
```

In session B:

```sql
UPDATE wallets SET balance = balance + 1
WHERE account_id = (SELECT account_id FROM wallets WHERE account_type = 'USER' LIMIT 1);
COMMIT;
```

At `READ COMMITTED`, session A sees the committed value on a second SELECT.
At `REPEATABLE READ`, session A continues to see its original snapshot:

```sql
SET TRANSACTION ISOLATION LEVEL REPEATABLE READ;
SELECT balance FROM wallets WHERE account_id = 1;
-- output remains the value from the first snapshot
ROLLBACK;
```

At `SERIALIZABLE`, two sessions that both read and then update based on that
read cause one commit to fail with:

```text
ERROR: could not serialize access due to concurrent update
```

The application transfer path uses row locks rather than relying on a
serialization retry loop.
