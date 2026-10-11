# Five-minute demo script

This is the reproducible script for recording the required five-minute video.
Run `docker compose up --build` first.

1. Create or seed two users and wallets, then POST `/transfers` with a fresh
   `Idempotency-Key`. Show the created transfer and balances.
2. Repeat the exact request with the same key. Show that the response is
   replayed and no second transfer is created.
3. Insert an enabled `risk_rules` row with a low `threshold_amount`, submit a
   transfer over that threshold, and show the resulting `risk_flags` row.
4. POST `/admin/reconciliation/run` with an idempotency key and show the
   returned `run_id` and persisted reconciliation run.
5. POST `/users/{id}/statement/ask` with a question such as “How much did I
   send in the last 30 days?” and show the paise total plus transfer IDs.

The prompt-injection test demonstrates that statement queries remain restricted
to the authenticated caller even when untrusted text contains instructions.
