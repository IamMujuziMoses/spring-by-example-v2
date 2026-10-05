# Spring Data Transactions

This example demonstrates how Spring manages database transactions using `@Transactional`.

The example models a simple money transfer between two accounts and shows how a transaction keeps related database changes consistent.

## What This Example Demonstrates

- `@Transactional`
- Transaction boundaries
- Atomic database operations
- Successful transactions
- Transaction rollback
- Exception handling within transactions
- Persisting multiple entities within one transaction
- Using `BigDecimal` for monetary values
- Testing transactional behavior

---

## Domain Model

The example uses two entities: `Account` and `Transfer`.

### Account

An `Account` represents a bank account with:

- An identifier
- An owner
- A balance

The balance can be changed through:

```java
deposit(BigDecimal amount)
```

and:

```java
withdraw(BigDecimal amount)
```

The `withdraw` operation prevents an account from being overdrawn by throwing an exception when there are insufficient funds.

### Transfer

A `Transfer` records a completed transfer between two accounts.

It contains:

- The source account
- The destination account
- The transferred amount

The relationships are represented using JPA:

```text
Transfer
   │
   ├── fromAccount ──> Account
   │
   └── toAccount ────> Account
```

---

## Repository Layer

The example uses Spring Data repositories:

```java
public interface AccountRepository extends CrudRepository<Account, Long> {
}
```

and:

```java
public interface TransferRepository extends CrudRepository<Transfer, Long> {
}
```

The repositories provide the persistence operations required by the transaction service without requiring custom repository implementations.

---

## Transaction Boundary

The transaction boundary is defined at the service layer:

```java
@Transactional
public void transfer(Long fromAccountId, Long toAccountId, BigDecimal amount) {
    ...
}
```

This is important because a transfer consists of multiple database operations.

A successful transfer performs several changes:

```text
Load source account
        ↓
Load destination account
        ↓
Withdraw from source
        ↓
Deposit into destination
        ↓
Save accounts
        ↓
Save transfer
        ↓
Commit transaction
```

All of these operations belong to the same transaction.

---

## Why Use a Transaction?

Consider a transfer of `25.00`:

```text
John: 100.00
Jane:  50.00
```

After the transfer:

```text
John: 75.00
Jane: 75.00
```

A transfer should not leave the database in a partially updated state.

For example, this would be incorrect:

```text
John: 75.00
Jane: 50.00
```

The money was removed from John but never added to Jane.

Likewise, the transfer record should not be persisted independently from the account updates.

The transaction ensures that these operations succeed or fail together.

---

## Successful Transaction

The `transfer` method performs the complete operation:

```java
fromAccount.withdraw(amount);
toAccount.deposit(amount);

accountRepository.save(fromAccount);
accountRepository.save(toAccount);

transferRepository.save(new Transfer(fromAccount, toAccount, amount));
```

Because the method is transactional, Spring manages these operations as one unit of work.

If everything succeeds, the transaction is committed.

The test verifies both the updated account balances and the persisted transfer:

```text
Source account       → 75.00
Destination account  → 75.00
Transfer records     → 1
Transfer amount      → 25.00
```

---

## Insufficient Funds

The `Account` entity prevents withdrawals when there is not enough money:

```java
if (balance.compareTo(amount) < 0) {
    throw new IllegalStateException("Insufficient funds");
}
```

For example:

```text
John: 10.00
Jane: 50.00
Transfer: 25.00
```

The withdrawal fails because John only has `10.00`.

The test verifies that:

- The exception is thrown.
- John's balance remains `10.00`.
- Jane's balance remains `50.00`.
- No transfer record is created.

This demonstrates that the transfer cannot proceed when the business operation itself is invalid.

---

## Transaction Rollback

The most important example is `transferAndFail`.

The method intentionally throws an exception after modifying the accounts and saving the transfer:

```java
fromAccount.withdraw(amount);
toAccount.deposit(amount);

accountRepository.save(fromAccount);
accountRepository.save(toAccount);

transferRepository.save(new Transfer(fromAccount, toAccount, amount));

throw new IllegalStateException("Transfer failed");
```

At first, the operation appears to have completed:

```text
John: 100.00 → 75.00
Jane:  50.00 → 75.00
Transfer: saved
```

However, the exception occurs before the transaction completes.

Because the method is transactional, Spring rolls back the transaction.

The final database state is therefore:

```text
John: 100.00
Jane: 50.00
Transfers: 0
```

This demonstrates the atomic nature of the transaction:

```text
Begin Transaction
       ↓
Debit source
       ↓
Credit destination
       ↓
Save transfer
       ↓
Exception
       ↓
Rollback
       ↓
Original database state
```

---

## Testing Transactional Behavior

The tests use `@SpringBootTest` rather than `@DataJpaTest`.

This is intentional.

The purpose of this example is to test the transaction boundary created by Spring around the service method.

Using:

```java
@SpringBootTest
```

allows Spring to create the actual `TransferService` bean and apply its `@Transactional` behavior through Spring's transaction infrastructure.

The rollback test therefore exercises the real transaction boundary:

```text
Test
 ↓
TransferService proxy
 ↓
@Transactional
 ↓
TransferService
 ↓
Repositories
 ↓
Database
```

This is different from manually creating the service with `new TransferService(...)`, which would bypass Spring's transactional proxy.

---

## Test Scenarios

The test suite covers three scenarios.

### 1. Successful Transfer

Verifies that:

- The source account is debited.
- The destination account is credited.
- A transfer record is created.
- The transfer references the correct accounts.
- The transfer amount is persisted correctly.

### 2. Insufficient Funds

Verifies that:

- An account cannot transfer more money than it has.
- The appropriate exception is thrown.
- Account balances remain unchanged.
- No transfer record is created.

### 3. Transaction Rollback

Verifies that:

- Multiple database changes can occur inside a transaction.
- An exception can cause the entire transaction to roll back.
- Account changes are reverted.
- The transfer record is not persisted.

---

## Important Transaction Concept

The key idea demonstrated by this example is **atomicity**.

A transfer is not a collection of unrelated database operations.

It is one logical business operation:

```text
Transfer Money
      │
      ├── Debit source
      ├── Credit destination
      └── Record transfer
```

These operations should be treated as a single unit.

If the operation succeeds:

```text
All changes → Commit
```

If the operation fails:

```text
All changes → Rollback
```

This is one of the primary reasons transaction boundaries are usually placed around service-layer business operations.

---

## What This Example Does Not Cover

This example intentionally keeps the transaction topic focused.

It does not cover:

- Transaction propagation
- Isolation levels
- Rollback rules
- Programmatic transactions
- `TransactionTemplate`
- Transaction synchronization
- Distributed transactions
- Nested transactions
- Transaction event listeners

These concepts can be explored in separate examples without making this example unnecessarily complex.

---

## Key Takeaways

After completing this example, you should understand:

1. How `@Transactional` defines a transaction boundary.
2. Why business operations involving multiple database changes should often be transactional.
3. How successful operations are committed.
4. How exceptions can cause transactions to roll back.
5. Why atomicity is important for financial operations.
6. Why transaction behavior should be tested through the Spring-managed service.
7. Why `BigDecimal` is appropriate for representing monetary amounts.

---

## Summary

This example demonstrates Spring Data transactions through a simple money-transfer scenario.

The important relationship is:

```text
Business Operation
        ↓
   @Transactional
        ↓
 Multiple Database Operations
        ↓
 ┌───────────────┐
 │               │
Success        Failure
 │               │
Commit         Rollback
 │               │
All changes    No changes
persist        persist
```

The goal is not to build a complete banking system, but to provide a small, focused example that makes Spring's transaction management easier to understand.

---

## Next

The next example will explore **Pagination**.
