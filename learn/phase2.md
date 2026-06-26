# Phase 2 — Exception Handling & Validation

This phase adds production-quality error handling and input validation to SecureBankPro.

Phase 1 built the OOP foundation. Phase 2 makes that foundation safe: it ensures that bad inputs, invalid states, and business-rule violations are caught, described clearly, and handled gracefully instead of crashing the application.

## What Was Implemented

```text
src/main/java/com/securebank/pro/
├── exception/
│   ├── InsufficientBalanceException.java   ← unchecked, balance guard
│   ├── InvalidAccountException.java        ← unchecked, account state guard
│   ├── UnauthorizedAccessException.java    ← unchecked, auth guard
│   ├── UserNotFoundException.java          ← unchecked, user lookup guard
│   └── TransactionFailedException.java     ← CHECKED, transfer failure wrapper
├── validation/
│   ├── AccountValidator.java               ← validateAccountStatus()
│   ├── PasswordValidator.java              ← validateEmail(), validatePassword()
│   └── TransactionValidator.java           ← validateTransferAmount()
└── entity/
    ├── Account.java                        ← updateBalance() now throws InsufficientBalanceException
    └── User.java                           ← registerUser() validates, authenticateOrThrow() added
```

Updated service:

```text
service/
├── BankService.java                        ← transferMoney now declares throws TransactionFailedException
└── impl/
    └── SimpleBankService.java              ← uses validators, catch-and-rethrow pattern
```

---

## Java Exception Hierarchy

Before diving into each concept, it helps to understand how Java organizes exceptions:

```text
Throwable
├── Error               (serious JVM problems — never catch these)
│   ├── OutOfMemoryError
│   └── StackOverflowError
└── Exception
    ├── IOException     (CHECKED — must handle)
    ├── SQLException    (CHECKED — must handle)
    └── RuntimeException (UNCHECKED — optional to catch)
        ├── NullPointerException
        ├── IllegalArgumentException
        ├── InsufficientBalanceException   ← our custom unchecked
        ├── InvalidAccountException        ← our custom unchecked
        ├── UnauthorizedAccessException    ← our custom unchecked
        └── UserNotFoundException          ← our custom unchecked

TransactionFailedException extends Exception  ← our custom CHECKED
```

---

## Checked vs Unchecked Exceptions

This is the most important distinction in Java exception handling.

### Unchecked Exceptions (extends RuntimeException)

The compiler does NOT force you to handle them.

```java
// This compiles fine even without a try-catch:
bankService.withdraw(account, new BigDecimal("9999.00"));
// If balance is too low, InsufficientBalanceException is thrown at runtime.
```

Use unchecked exceptions for:
- Programming errors (`NullPointerException`, `IllegalArgumentException`)
- Business rule violations that occur at runtime
- Cases where every caller does not need to explicitly handle the error

All four of our domain exceptions are unchecked:

```java
// All extend RuntimeException:
public class InsufficientBalanceException extends RuntimeException { ... }
public class InvalidAccountException extends RuntimeException       { ... }
public class UnauthorizedAccessException extends RuntimeException   { ... }
public class UserNotFoundException extends RuntimeException         { ... }
```

### Checked Exceptions (extends Exception)

The compiler FORCES you to handle them. Your code will not compile without either:
1. A `try-catch` block around the call, OR
2. A `throws` declaration in your own method signature

`TransactionFailedException` is our only checked exception:

```java
// This extends Exception (not RuntimeException):
public class TransactionFailedException extends Exception { ... }
```

Because `BankService.transferMoney` declares `throws TransactionFailedException`:

```java
Transaction transferMoney(Account source, Account dest, BigDecimal amount)
        throws TransactionFailedException;
```

This code will NOT compile:

```java
// Compiler error: Unhandled exception type TransactionFailedException
bankService.transferMoney(savings, current, amount);
```

You MUST write either:

```java
// Option 1: Handle it with try-catch
try {
    bankService.transferMoney(savings, current, amount);
} catch (TransactionFailedException e) {
    System.out.println("Transfer failed: " + e.getMessage());
}
```

```java
// Option 2: Declare it in your method (pass responsibility up)
public void processPayment() throws TransactionFailedException {
    bankService.transferMoney(savings, current, amount);
}
```

Why use a checked exception for transfer?

A failed transfer is a business failure that callers must plan for explicitly. It is not a bug — it is a real scenario (bad balance, inactive account, over limit) that the system should handle gracefully, not silently ignore.

---

## Custom Exceptions

Custom exceptions give your errors a name, a type, and context. This makes catching and debugging far more meaningful than a generic `RuntimeException("something went wrong")`.

### InsufficientBalanceException

Guards the balance against going negative. Thrown inside `Account.updateBalance()`.

```java
public class InsufficientBalanceException extends RuntimeException {

    private final String accountNumber;
    private final BigDecimal currentBalance;
    private final BigDecimal requestedAmount;

    // Simple message constructor
    public InsufficientBalanceException(String message) { ... }

    // Detailed constructor — builds the message automatically
    public InsufficientBalanceException(String accountNumber,
                                        BigDecimal currentBalance,
                                        BigDecimal requestedAmount) {
        super(String.format(
            "Insufficient balance in account '%s'. Available: %.2f, Requested: %.2f.",
            accountNumber, currentBalance, requestedAmount
        ));
        ...
    }

    // Cause-chaining constructor — wraps another exception
    public InsufficientBalanceException(String message, Throwable cause) { ... }
}
```

Where it is thrown — inside `Account.java`:

```java
public void updateBalance(BigDecimal amount) {
    BigDecimal newBalance = balance.add(amount);
    if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
        throw new InsufficientBalanceException(accountNumber, balance, amount.abs());
    }
    balance = newBalance;
}
```

Before Phase 2, this threw a generic `IllegalArgumentException("Balance cannot become negative.")`. Now it throws a named exception with full account context. Catching code can extract specific data:

```java
try {
    bankService.withdraw(savingsAccount, new BigDecimal("9999.00"));
} catch (InsufficientBalanceException e) {
    System.out.println(e.getMessage());        // readable message
    System.out.println(e.getAccountNumber());  // "SBP1001"
    System.out.println(e.getCurrentBalance()); // 1000.00
    System.out.println(e.getRequestedAmount()); // 9999.00
}
```

### InvalidAccountException

Guards against operations on inactive or null accounts. Thrown by `AccountValidator`.

```java
public class InvalidAccountException extends RuntimeException {
    private final String accountNumber;

    public InvalidAccountException(String message) { ... }

    public InvalidAccountException(String accountNumber, String reason) {
        super("Account '" + accountNumber + "' is invalid: " + reason);
        ...
    }
}
```

### UnauthorizedAccessException

Thrown when login fails or a user tries to perform an unauthorized action.

```java
public class UnauthorizedAccessException extends RuntimeException {
    private final String userEmail;
    private final String action;

    public UnauthorizedAccessException(String userEmail, String action) {
        super("User '" + userEmail + "' is not authorized to perform action: '" + action + "'.");
        ...
    }
}
```

### UserNotFoundException

Used when a lookup fails — the user does not exist.

```java
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String fieldName, String value) {
        super("No user found with " + fieldName + " = '" + value + "'.");
        ...
    }
}
```

Example usage (for future database phase):

```java
User user = userRepository.findByEmail("ghost@nowhere.com");
if (user == null) {
    throw new UserNotFoundException("email", "ghost@nowhere.com");
}
```

### TransactionFailedException (Checked)

Wraps transfer failures into a single checked exception. Callers must handle it.

```java
public class TransactionFailedException extends Exception {
    private final String referenceId;

    public TransactionFailedException(String message) { ... }

    // Cause-chaining — preserves the original exception
    public TransactionFailedException(String message, Throwable cause) {
        super(message, cause);
        ...
    }
}
```

---

## Try-Catch

`try-catch` intercepts exceptions and handles them without crashing the program.

Basic structure:

```java
try {
    // code that might throw
} catch (ExceptionType name) {
    // handle the exception
}
```

Example from `SecureBankProApplication.java`:

```java
try {
    bankService.withdraw(savingsAccount, new BigDecimal("9999.00"));
} catch (InsufficientBalanceException e) {
    System.out.println("Caught: " + e.getMessage());
}
```

What happens:

1. Java attempts `withdraw`.
2. Inside, `account.updateBalance(amount.negate())` detects the balance would go negative.
3. `InsufficientBalanceException` is thrown.
4. Execution jumps out of the `try` block immediately.
5. The `catch` block receives the exception object as `e`.
6. The message is printed. The program continues normally.

Without the `try-catch`, the program would crash and print a full stack trace.

---

## Finally

The `finally` block always executes — whether or not an exception was thrown.

```java
try {
    // code that might throw
} catch (SomeException e) {
    // handle error
} finally {
    // ALWAYS runs — exception or no exception
}
```

Example from the app:

```java
try {
    bankService.deposit(savingsAccount, new BigDecimal("250.00"));
    System.out.println("Deposit succeeded.");
} catch (InvalidAccountException e) {
    System.out.println("Deposit failed: " + e.getMessage());
} finally {
    System.out.println("[Finally] Deposit operation complete.");
}
```

The `finally` block runs in all three scenarios:
1. No exception: try runs, catch is skipped, finally runs.
2. Caught exception: try partially runs, catch runs, finally runs.
3. Uncaught exception: try partially runs, finally runs, then the exception propagates.

Real-world uses of `finally`:
- Closing a database connection
- Releasing a file handle
- Unlocking a shared resource
- Logging that an operation was attempted

---

## Throw

The `throw` keyword manually fires an exception from code.

```java
throw new SomeException("message");
```

Example in `User.java`:

```java
public void authenticateOrThrow(String email, String password) {
    if (!loginUser(email, password)) {
        throw new UnauthorizedAccessException(email, "login");
    }
}
```

When the credentials are wrong, `loginUser` returns false.

The method then uses `throw` to create and fire an `UnauthorizedAccessException`.

Execution leaves `authenticateOrThrow` immediately. The exception travels up the call stack until it is caught or crashes the program.

Difference between returning false and throwing:

```java
// Option 1: Return false — caller may silently ignore it
boolean ok = customer.loginUser("wrong", "wrong");
// ok is false but nothing forces the caller to check it

// Option 2: Throw — caller MUST deal with the failure
customer.authenticateOrThrow("wrong", "wrong");
// If caller doesn't catch UnauthorizedAccessException, the program crashes
```

---

## Throws

The `throws` keyword in a method signature tells callers what checked exceptions they must prepare for.

```java
public ReturnType methodName(params) throws CheckedException { ... }
```

In `BankService.java`:

```java
Transaction transferMoney(Account source, Account dest, BigDecimal amount)
        throws TransactionFailedException;
```

This is a contract. It says: "I, transferMoney, might throw TransactionFailedException. You, the caller, are responsible for handling it."

In `SimpleBankService.java`, the implementation honors this:

```java
@Override
public Transaction transferMoney(Account source, Account dest, BigDecimal amount)
        throws TransactionFailedException {
    try {
        // ... transfer logic
    } catch (InsufficientBalanceException | InvalidAccountException | IllegalArgumentException e) {
        throw new TransactionFailedException("Transfer failed: " + e.getMessage(), e);
    }
}
```

### Throw vs Throws

| Keyword | Where it goes | What it does |
|---|---|---|
| `throw` | Inside a method body | Fires a specific exception at that line |
| `throws` | In the method signature | Declares which checked exceptions may escape |

```java
// throws in signature — declaration
public void transfer() throws TransactionFailedException {
    // throw in body — action
    throw new TransactionFailedException("reason");
}
```

---

## Validation Methods

Validation classes live in the `validation` package. Each class handles one domain area.

### PasswordValidator — validateEmail()

```java
public static void validateEmail(String email) {
    if (email == null || email.isBlank()) {
        throw new IllegalArgumentException("Email cannot be null or blank.");
    }
    if (!email.matches(EMAIL_REGEX)) {
        throw new IllegalArgumentException(
            "Invalid email format: '" + email + "'. Expected format: user@domain.com"
        );
    }
}
```

Tests:

```java
PasswordValidator.validateEmail("not-an-email");
// Throws: Invalid email format: 'not-an-email'. Expected format: user@domain.com

PasswordValidator.validateEmail("nishant@example.com");
// No exception — email is valid
```

### PasswordValidator — validatePassword()

Enforces four rules, one check at a time:

```java
public static void validatePassword(String password) {
    if (password == null || password.isBlank()) {
        throw new IllegalArgumentException("Password cannot be null or blank.");
    }
    if (password.length() < 8) {
        throw new IllegalArgumentException("Password must be at least 8 characters long...");
    }
    if (!password.matches(".*[A-Z].*")) {
        throw new IllegalArgumentException("Password must contain at least one uppercase letter (A-Z).");
    }
    if (!password.matches(".*[0-9].*")) {
        throw new IllegalArgumentException("Password must contain at least one digit (0-9).");
    }
    if (!password.matches(".*[!@#$%^&*...].*")) {
        throw new IllegalArgumentException("Password must contain at least one special character.");
    }
}
```

Checking each rule separately means the error message tells the user exactly what they need to fix. A single combined regex would only say "invalid password" with no guidance.

Tests:

```java
PasswordValidator.validatePassword("weak");
// Throws: Password must be at least 8 characters long. Provided length: 4.

PasswordValidator.validatePassword("Password1");
// Throws: Password must contain at least one special character (e.g. @, #, !, %).

PasswordValidator.validatePassword("Pass@123");
// No exception — all rules satisfied
```

### TransactionValidator — validateTransferAmount()

```java
public static void validateTransferAmount(BigDecimal amount) {
    if (amount == null) {
        throw new IllegalArgumentException("Transaction amount cannot be null.");
    }
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
        throw new IllegalArgumentException("Transaction amount must be greater than zero...");
    }
    if (amount.stripTrailingZeros().scale() > 2) {
        throw new IllegalArgumentException("Transaction amount cannot have more than 2 decimal places...");
    }
    if (amount.compareTo(MAX_TRANSACTION_LIMIT) > 0) {
        throw new IllegalArgumentException("Transaction amount exceeds the per-transaction limit...");
    }
}
```

Note `stripTrailingZeros().scale()` — this handles values like `100.000` which has scale 3 but is effectively 2. Without stripping, it would incorrectly reject `100.000`.

### AccountValidator — validateAccountStatus()

```java
public static void validateAccountStatus(Account account) {
    if (account == null) {
        throw new InvalidAccountException("Account reference cannot be null.");
    }
    if (!account.isActive()) {
        throw new InvalidAccountException(
            account.getAccountNumber(),
            "Account is not active. Call createAccount() before performing transactions."
        );
    }
}
```

This is called at the start of `deposit`, `withdraw`, and `transferMoney` — before any balance logic runs. It is a guard clause.

---

## Exception Constructors — Three Patterns

All five custom exceptions provide three constructor patterns:

### Pattern 1: Simple message

```java
throw new InsufficientBalanceException("Not enough funds.");
```

Use when you only have a message and no additional data.

### Pattern 2: Contextual data

```java
throw new InsufficientBalanceException("SBP1001", currentBalance, requestedAmount);
```

The exception builds its own descriptive message from the provided data. Catch code can call `getAccountNumber()`, `getCurrentBalance()`, `getRequestedAmount()` to extract details for logging or UI display.

### Pattern 3: Cause chaining

```java
throw new TransactionFailedException("Transfer failed: " + e.getMessage(), e);
```

The second argument to `super(message, cause)` stores the original exception inside this new exception. Calling `e.getCause()` on the outer exception returns the inner one.

This is important for debugging. The full chain of causes appears in the stack trace.

Example:

```java
try {
    bankService.transferMoney(savings, current, new BigDecimal("-50.00"));
} catch (TransactionFailedException e) {
    System.out.println(e.getMessage());           // Transfer of -50.00 from ... failed: ...
    System.out.println(e.getCause().getClass());  // IllegalArgumentException
    System.out.println(e.getCause().getMessage()); // Transaction amount must be greater than zero.
}
```

---

## Multiple Catch Blocks

One `try` block can have many `catch` blocks, one for each exception type you want to handle differently.

```java
try {
    bankService.withdraw(currentAccount, new BigDecimal("99999.00"));
} catch (InsufficientBalanceException e) {
    // Most specific — catch this first
    System.out.println("Insufficient balance: " + e.getMessage());
} catch (InvalidAccountException e) {
    System.out.println("Invalid account: " + e.getMessage());
} catch (RuntimeException e) {
    // Most general — catch everything else here
    System.out.println("Unexpected error: " + e.getMessage());
}
```

Java evaluates catch blocks from top to bottom. The first matching type wins.

Rule: more specific exceptions must come before more general ones.

This does not compile (compiler detects unreachable catch):

```java
} catch (RuntimeException e) {         // too general — catches everything
} catch (InsufficientBalanceException e) { // unreachable — never reached
```

---

## Catch and Re-Throw Pattern

`SimpleBankService.transferMoney` demonstrates this pattern:

```java
public Transaction transferMoney(Account source, Account dest, BigDecimal amount)
        throws TransactionFailedException {
    try {
        // All the real work — can throw multiple unchecked exceptions
        AccountValidator.validateAccountStatus(source);
        AccountValidator.validateAccountStatus(dest);
        TransactionValidator.validateTransferAmount(amount);
        source.updateBalance(amount.negate());
        dest.updateBalance(amount);
        ...
    } catch (InsufficientBalanceException | InvalidAccountException | IllegalArgumentException e) {
        // Caught unchecked, re-thrown as checked
        throw new TransactionFailedException(
            "Transfer of " + amount + " from '" + source.getAccountNumber()
            + "' to '" + dest.getAccountNumber() + "' failed: " + e.getMessage(),
            e   // ← original exception preserved as cause
        );
    }
}
```

The `|` in the catch is a multi-catch — one catch block handles multiple exception types. Java 7+.

Why re-throw?

1. The caller sees one consistent failure type (`TransactionFailedException`) instead of three different unchecked types.
2. The original root cause is still accessible via `getCause()`.
3. A checked exception forces callers to acknowledge the failure.

---

## Integration with Existing Phase 1 Code

### Account.updateBalance — before and after

Before Phase 2:

```java
public void updateBalance(BigDecimal amount) {
    BigDecimal newBalance = balance.add(amount);
    if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
        throw new IllegalArgumentException("Balance cannot become negative.");
    }
    balance = newBalance;
}
```

After Phase 2:

```java
public void updateBalance(BigDecimal amount) {
    BigDecimal newBalance = balance.add(amount);
    if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
        throw new InsufficientBalanceException(accountNumber, balance, amount.abs());
    }
    balance = newBalance;
}
```

The logic is identical. The only change is the exception type and the data it carries. Callers that caught `IllegalArgumentException` before now need to catch `InsufficientBalanceException` — which is a subtype of `RuntimeException`, so it is still unchecked.

### User.registerUser — before and after

Before Phase 2:

```java
public boolean registerUser() {
    if (isBlank(fullName) || isBlank(email) || isBlank(password)) {
        return false;
    }
    registered = true;
    return true;
}
```

After Phase 2:

```java
public boolean registerUser() {
    if (isBlank(fullName)) {
        System.out.println("[Registration] Full name cannot be blank.");
        return false;
    }
    try {
        PasswordValidator.validateEmail(email);
        PasswordValidator.validatePassword(password);
    } catch (IllegalArgumentException e) {
        System.out.println("[Registration] Validation failed: " + e.getMessage());
        return false;
    }
    registered = true;
    return true;
}
```

The method now validates format rules, not just blank checks. The return type and behavior contract are unchanged.

---

## Program Output (Phase 2 Run)

```text
============================================================
  SecureBankPro — Phase 2: Exception Handling & Validation
============================================================

──── 1. Input Validation ────
[Email] Testing invalid: 'not-an-email'
  Error: Invalid email format: 'not-an-email'. Expected format: user@domain.com
[Email] Testing valid: 'nishant@example.com'
  Valid email confirmed.
[Password] Testing weak: 'weak'
  Error: Password must be at least 8 characters long. Provided length: 4.
[Password] Testing no-special-char: 'Password1'
  Error: Password must contain at least one special character (e.g. @, #, !, %).
[Password] Testing strong: 'Pass@123'
  Valid password confirmed.
[Amount] Testing zero: 0.00
  Error: Transaction amount must be greater than zero. Provided: 0.
[Amount] Testing over-limit: 200000.00
  Error: Transaction amount of 200000.00 exceeds the per-transaction limit of 100000.00.
[Amount] Testing valid: 500.00
  Valid amount confirmed.

──── 2. Registration with Validation ────
Customer registered: true
[Registration] Validation failed: Password must be at least 8 characters long. Provided length: 4.
Bad user registered: false

──── 3. Try-Catch: Unchecked InsufficientBalanceException ────
Caught InsufficientBalanceException:
  Insufficient balance in account 'SBP1001'. Available: 1000.00, Requested: 9999.00.
  Account : SBP1001
  Balance : 1000.00
  Requested: 9999.00

──── 4. Finally Block — Always Executes ────
Deposit succeeded. New savings balance: 1250.00
[Finally] Deposit operation complete — balance confirmed or rollback required.

──── 5. Checked Exception: TransactionFailedException ────
Transfer succeeded.
  Savings after transfer: 950.00
  Current after transfer: 800.00
[Finally] Transfer attempt logged.

──── 6. throw Keyword — UnauthorizedAccessException ────
Caught UnauthorizedAccessException:
  User 'wrong@email.com' is not authorized to perform action: 'login'.
Authentication succeeded for: Nishant Sharma

──── 7. InvalidAccountException — Operating on Inactive Account ────
Caught InvalidAccountException:
  Account 'SBP9999' is invalid: Account is not active. Call createAccount() before performing transactions.

──── 8. Multiple Catch Blocks ────
Specific catch — InsufficientBalance: Insufficient balance in account 'SBP2001'. Available: 800.00, Requested: 99999.00.

──── 9. Catch-and-Rethrow — Transfer with Invalid Amount ────
Caught TransactionFailedException (checked):
  Transfer of -50.00 from 'SBP1001' to 'SBP2001' failed: Transaction amount must be greater than zero. Provided: -50.00.
  Root cause type : IllegalArgumentException
  Root cause message: Transaction amount must be greater than zero. Provided: -50.00.

──── Final Account State ────
Welcome, Nishant Sharma
Admin dashboard for Bank Admin (Operations)
Savings balance : 950.00
Current balance : 800.00
Savings transactions: 2
```

---

## Summary of All Phase 2 Concepts

| Concept | Keyword / Mechanism | Example in Project |
|---|---|---|
| Try-Catch | `try { } catch (E e) { }` | Catching `InsufficientBalanceException` on withdraw |
| Finally | `finally { }` | Logging after deposit and transfer attempts |
| Throw | `throw new X(...)` | `authenticateOrThrow` in `User.java` |
| Throws | `method() throws X` | `BankService.transferMoney` declares `throws TransactionFailedException` |
| Checked exception | `extends Exception` | `TransactionFailedException` — compiler enforces handling |
| Unchecked exception | `extends RuntimeException` | `InsufficientBalanceException`, `InvalidAccountException`, etc. |
| Custom exception | Own class extending exception base | All five exception classes in `exception/` package |
| Cause chaining | `super(message, cause)` / `getCause()` | `TransactionFailedException` wrapping an `InsufficientBalanceException` |
| Multiple catch | `catch (A e) catch (B e)` | `withdraw` demo catching 3 exception types |
| Multi-catch | `catch (A | B e)` | `SimpleBankService.transferMoney` |
| Catch and re-throw | Catch unchecked, throw checked | `transferMoney` wrapping all failures into `TransactionFailedException` |
| Input validation | Static methods throwing on failure | `PasswordValidator`, `AccountValidator`, `TransactionValidator` |
