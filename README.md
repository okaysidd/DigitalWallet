# Digital Wallet – Transaction Handling Overview

This project contains **two alternative service implementations** for transferring money between wallets:

1. `TransactionService.java`
2. `TransactionServiceWithDBHandling.java`

Both classes expose the same business method:

```java
Boolean sendMoneyBetweenWallets(TransferMoneyBetweenWalletsRequest request)
```

but they differ significantly in **how** they protect consistency and isolation when multiple requests hit the service at the same time.

---

## 1. TransactionService.java  – Application-Level (In-Memory) Locking

| Aspect | Implementation details |
|--------|------------------------|
| Lock acquisition | Calls `wallet.lock()` (a boolean flag stored on the `Wallet` entity) **after** the entity has been loaded from the database. |
| Persistence | Immediately `save(sendingWallet)` / `save(receivingWallet)` so the updated *locked* flag is persisted. |
| Validation | Verifies the sender has enough balance and the receiver will not exceed the maximum allowed balance *before* locking. |
| Error handling | If both wallets are not locked, or an exception is thrown, a failed `TransactionEntry` is persisted. |
| Lock release | `unlock()` is executed in a `finally` block and persisted. |

### Why this approach breaks under high concurrency

1. **Race window while reading:** Two threads (T₁ & T₂) can *simultaneously* load the same wallet rows **before** either thread sets the `locked` flag. Each thread therefore believes the wallets are *unlocked* and proceeds.
2. **Non-transactional flag:** The `locked` boolean is just a column. The JPA provider does *not* hold any row-level or table-level lock, so the flag does not prevent concurrent updates inside a single database transaction.
3. **Lost update:** Both threads may subtract the same amount from the sender's balance (or add to the receiver) and commit, leading to an inconsistent final balance.

In short, this implementation provides only *logical* locking that relies on every concurrent caller respecting the flag. A second caller reading stale data will ignore the flag and corrupt the balance.

---

## 2. TransactionServiceWithDBHandling.java  – Database Row Locking with Retry

| Aspect | Implementation details |
|--------|------------------------|
| Lock acquisition | Calls `walletService.getWalletWithLock(id)` which loads the wallet using **`PESSIMISTIC_WRITE`** lock (`SELECT … FOR UPDATE`). The database guarantees exclusive access to the row for the whole transaction. |
| Spring annotations | `@Transactional` – wraps the entire operation in a single DB transaction.<br>`@Retryable` – on lock-related exceptions (e.g. `CannotAcquireLockException`, `PessimisticLockException`) Spring will retry up to **3** times with exponential back-off (200 ms, 400 ms, 800 ms). |
| Validation | Same balance checks as the basic service – now executed **while the rows are locked**. |
| Atomic update | The debit and credit happen inside the same DB transaction. Either both succeed or both are rolled back. |
| Failure handling | On `RuntimeException` a failed `TransactionEntry` is persisted and the transaction is rolled back. |

### Advantages under concurrency

* **No stale read:** The moment a wallet row is selected `FOR UPDATE`, any other concurrent transaction that attempts the same will block (or throw) until the first transaction completes.
* **No lost update:** Because only one transaction can update the row at a time, balances stay correct.
* **Automatic retry:** Short-lived contention is resolved by retrying instead of failing the request outright.

---

## Concurrent Scenario Example

Imagine two clients simultaneously try to transfer ₹1 000 from Wallet A to Wallet B.

| Step | Application-Level Lock (`TransactionService`) | Database Row Lock (`TransactionServiceWithDBHandling`) |
|------|----------------------------------------------|-------------------------------------------------------|
| 1 | T₁ & T₂ both read Wallet A balance = 5 000 | T₁ acquires `FOR UPDATE` lock on Wallet A & B. T₂ blocks. |
| 2 | T₁ sets `locked = true` and saves. T₂ **still holds old entity** (locked = false). | — |
| 3 | Both subtract 1 000 → balance becomes 4 000 (T₁) and **3 000 (T₂)**. | T₁ completes, commits. |
| 4 | Database now stores **3 000** instead of 4 000. Funds were double-debited. | T₂ retries, re-reads fresh data, proceeds safely or aborts if balance insufficient. |

---

## Take-aways

1. **Prefer database locking for financial operations.** Application-level flags cannot guarantee isolation.
2. **Combine pessimistic locks with retries.** Short blocking times are normal; users should not pay the price of transient lock contention.
3. **Wrap multi-entity updates in one transaction.** Ensures atomicity – either both wallets are updated or none.

The `TransactionServiceWithDBHandling` class embodies these principles and should be the default choice for production workloads. 