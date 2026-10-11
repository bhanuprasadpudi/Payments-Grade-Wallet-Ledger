# Payments-grade wallet ledger

## Run it

Prerequisites: Docker Desktop and Docker Compose.

```bash
docker compose up --build
```

This starts PostgreSQL, the HTTP API on `http://localhost:8080`, and a separate
scheduled worker process. Flyway creates the schema from an empty database.

Seed the requested dataset after PostgreSQL is healthy:

```powershell
.\scripts\seed.ps1
```

The seed script creates 1,000 users, 2,000 wallets (1,000 user wallets and
three system accounts are included in the schema; rerun-safe user insertion),
and 100,000 transfers with balanced ledger rows.

## Design decisions

- Money is whole paise in PostgreSQL `BIGINT`; ledger amounts are positive and
  direction is represented by `DEBIT` or `CREDIT`.
- Balances are stored for fast reads and reconciled against the append-only
  ledger nightly. User wallets have a database non-negative constraint; system
  accounts may go negative.
- User wallets and system accounts share `wallets` because transfers and
  ledger entries then use one account foreign key and one locking path.
- Email and phone are business data, not primary keys: they can change and are
  not safe immutable identifiers.
- Transfers use `READ COMMITTED` plus ordered `FOR UPDATE` row locks. This
  serializes spends on the same wallets without the throughput and retry cost
  of `SERIALIZABLE`.
- Idempotency uses a per-user unique key and SHA-256 body hash. Completed
  responses are stored and replayed exactly.
- Statement questions are translated to a bounded application query; user
  text and transfer notes never become SQL.

See [docs/schema.md](docs/schema.md) and [docs/isolation.md](docs/isolation.md).

## Test

```bash
./mvnw test
```

CI runs the same command. PostgreSQL/Testcontainers tests execute when Docker
is available.

## API examples

All POST requests require `Idempotency-Key` and `X-User-Id` UUID headers.

```bash
curl -X POST http://localhost:8080/transfers \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: 11111111-1111-1111-1111-111111111111" \
  -H "X-User-Id: 22222222-2222-2222-2222-222222222222" \
  -d '{"sourceAccountId":1,"destinationAccountId":2,"amount":100}'
```

## Demo checklist

The five-minute demonstration should show one transfer, replay the same
idempotency key, trigger a configured fraud rule, call
`POST /admin/reconciliation/run`, and ask a statement question. The Docker
Compose stack provides the API and worker used for that walkthrough.

## Next steps

For production, replace the temporary caller header with OAuth/JWT
authentication, add explicit admin authorization, implement a provider-backed
LLM adapter behind a strict JSON schema, add cursor pagination, and publish
metrics/traces for lock waits, reconciliation anomalies, and fraud latency.
