# SecureBankPro — Core Concepts Study Guide

This guide compiles all concepts implemented in **SecureBankPro** across all six learning phases into one simple, clean, and educational document.

---

## ── Phase 1: Object-Oriented Programming (OOP) ──

### 1. Abstraction & Classes
Hiding implementation details and showing only essential features.
- **Abstract Class**: `Account` defines structural fields (`balance`, `accountNumber`) but cannot be instantiated directly.
- **Abstract Method**: `calculateMaintenanceFee()` has no body in `Account` and must be defined by subclasses.

### 2. Encapsulation
Bundling data (variables) and methods together, restricting direct access using private modifiers.
- **ReadOnly/Final Fields**: `userId` is marked `final` and set only in the constructor.
- **Access Control**: Balance is modified safely via `updateBalance(amount)` rather than exposing a direct setter.

### 3. Inheritance
Creating new classes (subclasses) from existing ones (superclasses) to reuse code.
- **Subclassing**: `SavingsAccount` and `CurrentAccount` extend `Account` to inherit general account properties.
- **Super Constructor**: `super(accountNumber, owner, openingBalance)` transfers variables to the parent.

### 4. Polymorphism
Performing a single action in different ways.
- **Method Overriding (`@Override`)**: `CurrentAccount` overrides `calculateMaintenanceFee()` to return `50.00` while `SavingsAccount` returns `0.00`.
- **Method Overloading**: `User` has `registerUser()` and `registerUser(name, email, password)` with different signatures.

### 5. Enums
Typesafe lists of constants representing choices.
- **Usage**: `Role` (`CUSTOMER`, `ADMIN`), `AccountType` (`SAVINGS`, `CURRENT`), `TransactionType` (`DEPOSIT`, `WITHDRAWAL`, `TRANSFER`).

---

## ── Phase 2: Exception Handling & Validation ──

### 1. Checked vs Unchecked Exceptions
- **Unchecked Exceptions (extends `RuntimeException`)**: Compilers do not force you to handle them (e.g. `InsufficientBalanceException`, `InvalidAccountException`). Use for business rules and program errors.
- **Checked Exceptions (extends `Exception`)**: Compile-time errors if not handled inside a `try-catch` or declared via `throws` (e.g. `TransactionFailedException`). Use for recovery scenarios.

### 2. Exception Keywords
- **`throw`**: Creates and throws an exception instance (e.g., `throw new UserNotFoundException("email", email)`).
- **`throws`**: Declares in a method signature that a checked exception may occur.
- **`finally`**: A block that runs *always* after try-catch (e.g., closing resources or logging attempts).

### 3. Catch-and-Rethrow Pattern
Catching low-level exceptions and wrapping them in a high-level checked exception to simplify API handling.
```java
try {
    sourceAccount.updateBalance(amount.negate());
} catch (InsufficientBalanceException e) {
    throw new TransactionFailedException("Transfer failed", e); // Chain original exception
}
```

### 4. Input Validation
Validating input formats before modifying states to prevent bad data entry.
- **Regex Format validation**: Matching email formats with a regular expression pattern.
- **Step-by-step checks**: Validating passwords for length, digits, uppercase, and special characters separately for clear feedback.

---

## ── Phase 3: Collections Framework ──

### 1. List (ArrayList)
An ordered, indexable array list that dynamically resizes as items are added.
- **Code Usage**: Storing transaction histories.
```java
List<Transaction> transactions = new ArrayList<>();
```

### 2. Map (HashMap)
Stores key-value pairs. Provides extremely fast $O(1)$ lookups by key.
- **Code Usage**: In-memory database repositories lookup.
```java
Map<Integer, User> usersById = new HashMap<>();
```

### 3. Set (HashSet)
A unique collection that rejects duplicates. Backed internally by a HashMap.
- **Code Usage**: Tracking active user session emails.
```java
Set<String> activeSessions = new HashSet<>();
```

### 4. Queue (LinkedList)
A FIFO (First-In, First-Out) structure where elements are inserted at the tail (`offer`) and removed from the head (`poll`).
- **Code Usage**: Login activity logs (last 10 elements) and transaction queues waiting to be processed.
```java
Queue<Transaction> pendingQueue = new LinkedList<>();
```

### 5. Iterator
An object that lets you step through a collection one by one. Safe from modification errors.
- **Code Usage**: Searching users by name or email.
```java
Iterator<User> iterator = usersById.values().iterator();
while (iterator.hasNext()) {
    User user = iterator.next();
    if (user.getFullName().toLowerCase().contains(query)) {
        results.add(user);
    }
}
```

### 6. Comparable vs Comparator
- **Comparable**: Integrated into the entity class using `compareTo()` to set the natural default sorting order (by Transaction ID).
- **Comparator**: Set externally using custom sorting algorithms (e.g., sort by amount descending or sort by date).
```java
// Comparable in Transaction entity
public int compareTo(Transaction other) {
    return Integer.compare(this.transactionId, other.transactionId);
}

// Comparator in Transaction entity
public static final Comparator<Transaction> BY_AMOUNT_DESC = 
    Comparator.comparing(Transaction::getAmount).reversed();
```

---

## ── Phase 4: File Handling & Logging ──

### 1. Character Streams (`FileReader` / `FileWriter`)
Reading and writing text files character-by-character or line-by-line.
- **Buffering**: `BufferedReader` and `BufferedWriter` wrap character streams to improve read/write efficiency by reading/writing blocks of characters instead of single bytes.
```java
try (BufferedWriter writer = new BufferedWriter(new FileWriter("logs/transactions.txt", true))) {
    writer.write(logLine);
    writer.newLine();
}
```

### 2. Object Serialization & Deserialization
Saving complete Java object graphs to files and loading them back into memory.
- **`Serializable` Interface**: A marker interface (no methods) that tells the JVM an object class is allowed to be serialized.
- **Stream Classes**: `ObjectOutputStream` writes objects; `ObjectInputStream` reads them.
```java
// Backup all database records
try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("backups/database.ser"))) {
    oos.writeObject(databaseBackupInstance);
}
```

### 3. Application Logging Basics
Using standard logging frameworks to record execution events, audit actions, and handle troubleshooting info.
- **Log Levels**: `INFO` for normal execution steps, `WARNING` for issues that do not crash the app, and `SEVERE` for exceptions.
```java
private static final Logger logger = Logger.getLogger("SecureBankPro");
logger.info("Database restore completed successfully.");
```

---

## ── Phase 5: SQL & Database Design ──

### 1. Primary & Foreign Keys
- **Primary Key (PK)**: A unique identifier for every row in a table (e.g., `userId` or `accountId`).
- **Foreign Key (FK)**: Establishes a link between tables (e.g., `ownerId` in `accounts` references `userId` in `users`). Enforces referential integrity.

### 2. Cascade Actions
Defines behavior when a referenced parent row is updated or deleted.
- **`ON DELETE CASCADE`**: Deleting a user automatically deletes all associated accounts.
- **`ON DELETE SET NULL`**: Deleting an account leaves associated transactions in the database but sets their account reference to null.

### 3. SQL Joins
Combines fields from two or more tables based on matching columns (e.g., fetching a transaction alongside its source account and destination account details in a single query).

### 4. Single-Table Inheritance
An OOP mapping pattern where all classes in an inheritance hierarchy are stored in a single table, using a special column (like `role` or `discriminator`) to identify the subclass of each row.

---

## ── Phase 6: JDBC Integration & Architecture ──

### 1. Data Access Object (DAO) Pattern
A structural design pattern that abstracts and encapsulates all access to the data source (database). Decouples services from SQL execution.
- **DAO Interfaces**: Define database methods (e.g., `saveUser()`, `updateBalance()`) without referencing JDBC types.
- **DAO Implementations**: Contain JDBC drivers and run the actual statements.

### 2. Prepared Statements (`PreparedStatement`)
An object representing a precompiled SQL statement.
- **Security**: Prevents SQL injection by separating code from user input parameters.
- **Performance**: Precompiles queries for faster database execution.

### 3. Connection Pooling
An optimization technique where a pool of active database connections is kept open and recycled.
- **Leasing**: Connection request takes a connection from the pool.
- **Recycling**: Closing the connection returns it to the pool instead of physically closing the socket.

### 4. JDBC Transactions (Commit & Rollback)
Ensures multiple database updates succeed or fail as a single atomic unit.
- **`setAutoCommit(false)`**: Suspends auto-commits to start a transaction context.
- **`commit()`**: Finalizes all transaction updates.
- **`rollback()`**: Reverts all transaction updates back to the start of the transaction if an exception occurs.
