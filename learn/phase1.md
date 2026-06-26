# Phase 1 - Java Fundamentals and Project Setup

This phase builds the core object-oriented foundation of SecureBankPro. The goal is not Spring Boot yet. The goal is to understand how plain Java classes, objects, inheritance, abstraction, interfaces, and methods work together to model a banking system.

## What Was Implemented

Phase 1 added these main domain and service classes:

```text
src/main/java/com/securebank/pro/
├── SecureBankProApplication.java
├── entity/
│   ├── User.java
│   ├── Admin.java
│   ├── Account.java
│   ├── SavingsAccount.java
│   ├── CurrentAccount.java
│   └── Transaction.java
├── service/
│   └── BankService.java
├── service/impl/
│   └── SimpleBankService.java
└── enums/
    ├── AccountType.java
    ├── Role.java
    └── TransactionType.java
```

The system now supports:

* User registration, login, and logout
* Admin as a special type of user
* Abstract bank account behavior
* Savings and current account types
* Deposits
* Withdrawals
* Money transfers
* Transaction creation
* Transaction history lookup

## Java Project Structure

The code is inside:

```text
src/main/java/com/securebank/pro
```

This follows the standard Maven Java project layout:

```text
src/main/java
```

That folder contains production Java code.

The package name is:

```java
package com.securebank.pro;
```

In Java, package names normally match the folder structure:

```text
com/securebank/pro
```

So this file:

```text
src/main/java/com/securebank/pro/entity/User.java
```

starts with:

```java
package com.securebank.pro.entity;
```

Packages help organize code by responsibility. In this project:

* `entity` contains business objects like `User`, `Account`, and `Transaction`.
* `service` contains interfaces that define business operations.
* `service.impl` contains concrete service implementations.
* `enums` contains fixed sets of values like roles and transaction types.

## Classes and Objects

A class is a blueprint. An object is a real instance created from that blueprint.

Example from `User.java`:

```java
public class User {
    private String fullName;
    private String email;
    private String password;
}
```

This class describes what a user has.

In `SecureBankProApplication.java`, we create an object:

```java
User customer = new User("Nishant Sharma", "nishant@example.com", "Pass@123");
```

Here:

* `User` is the class.
* `customer` is the object reference.
* `new User(...)` creates the actual object in memory.

## Constructors

A constructor is a special method used to create an object and initialize its state.

In `User.java`:

```java
public User(String fullName, String email, String password) {
    this(fullName, email, password, Role.CUSTOMER);
}
```

This constructor creates a normal customer user.

There is also a second constructor:

```java
protected User(String fullName, String email, String password, Role role) {
    this.userId = nextUserId++;
    this.fullName = fullName;
    this.email = email;
    this.password = password;
    this.role = role;
}
```

This constructor allows subclasses, like `Admin`, to set a different role.

Important idea:

```java
this(...)
```

calls another constructor in the same class.

In `Admin.java`:

```java
super(fullName, email, password, Role.ADMIN);
```

`super(...)` calls the parent class constructor.

## Access Modifiers

Access modifiers control visibility.

This project uses:

```java
public
private
protected
```

### public

Accessible from anywhere.

Example:

```java
public boolean registerUser()
```

Other classes can call this method.

### private

Accessible only inside the same class.

Example:

```java
private String password;
```

Other classes cannot directly access `password`.

This protects the object's internal data.

### protected

Accessible inside the same package and by subclasses.

Example:

```java
protected User(String fullName, String email, String password, Role role)
```

This allows `Admin` to call the constructor, but keeps it away from general public use.

## Encapsulation

Encapsulation means keeping data private and exposing controlled methods to interact with it.

In `User.java`, fields are private:

```java
private String fullName;
private String email;
private String password;
private boolean loggedIn;
```

External code cannot directly do this:

```java
customer.password = "weak";
```

Instead, the class provides methods:

```java
public boolean loginUser(String email, String password)
public void logoutUser()
public boolean isLoggedIn()
```

This means the object controls how its own state changes.

Banking example:

In `Account.java`, balance is private:

```java
private BigDecimal balance;
```

The balance changes through:

```java
public void updateBalance(BigDecimal amount)
```

That method prevents the balance from becoming negative:

```java
if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
    throw new IllegalArgumentException("Balance cannot become negative.");
}
```

That is strong encapsulation. The account protects itself from invalid state.

## Static Keyword

`static` means the field or method belongs to the class itself, not one individual object.

In `User.java`:

```java
private static int nextUserId = 1;
```

All `User` objects share this one counter.

Each new user receives a unique ID:

```java
this.userId = nextUserId++;
```

If you create three users:

```java
User u1 = new User(...); // userId 1
User u2 = new User(...); // userId 2
User u3 = new User(...); // userId 3
```

The counter is shared across all objects.

In `Account.java`, this method is static:

```java
public static Account createAccount(...)
```

You call it on the class:

```java
Account account = Account.createAccount(...);
```

This is a factory method. It decides which subclass object to create.

## Final Keyword

`final` means something cannot be reassigned or overridden, depending on where it is used.

In `User.java`:

```java
private final int userId;
```

Once a `userId` is assigned in the constructor, it cannot be changed.

In `Account.java`:

```java
public static final BigDecimal MIN_OPENING_BALANCE = BigDecimal.ZERO;
```

This is a constant.

In `User.java`:

```java
public final boolean hasRole(Role expectedRole)
```

This method cannot be overridden by subclasses. That protects role-checking behavior.

## Methods

Methods define behavior.

User methods:

```java
registerUser()
loginUser()
logoutUser()
```

Account methods:

```java
createAccount()
getBalance()
updateBalance()
```

Banking methods:

```java
deposit()
withdraw()
transferMoney()
```

Transaction methods:

```java
createTransaction()
getTransactionHistory()
```

A method usually has:

```java
accessModifier returnType methodName(parameters)
```

Example:

```java
public boolean loginUser(String email, String password)
```

Meaning:

* `public`: other classes can call it.
* `boolean`: it returns `true` or `false`.
* `loginUser`: method name.
* `String email, String password`: input parameters.

## Method Overloading

Method overloading means same method name, different parameters.

In `User.java`:

```java
public boolean registerUser()
```

and:

```java
public boolean registerUser(String fullName, String email, String password)
```

Both methods are named `registerUser`, but they accept different inputs.

Why this is useful:

* If the object already has user details, call `registerUser()`.
* If you want to update details during registration, call `registerUser(...)`.

In `Account.java`, `createAccount` is also overloaded:

```java
public static Account createAccount(AccountType accountType, String accountNumber, User owner)
```

and:

```java
public static Account createAccount(
        AccountType accountType,
        String accountNumber,
        User owner,
        BigDecimal openingBalance
)
```

One creates an account with zero opening balance. The other accepts an opening balance.

## Method Overriding

Method overriding means a child class provides its own version of a parent method.

In `User.java`:

```java
public String getDashboardMessage() {
    return "Welcome, " + fullName;
}
```

In `Admin.java`:

```java
@Override
public String getDashboardMessage() {
    return "Admin dashboard for " + getFullName() + " (" + department + ")";
}
```

Because `Admin extends User`, admin objects can replace the parent behavior.

The `@Override` annotation tells Java:

```text
This method should override a parent method.
```

If the method name or signature is wrong, Java gives a compile error. That is helpful.

## Inheritance

Inheritance lets one class reuse and specialize another class.

In `Admin.java`:

```java
public class Admin extends User
```

This means:

```text
Admin is a User.
```

So `Admin` gets user behavior:

```java
registerUser()
loginUser()
logoutUser()
getEmail()
getRole()
```

But `Admin` adds admin-specific behavior:

```java
canManageUsers()
getDepartment()
```

This creates a role-based object hierarchy:

```text
User
└── Admin
```

A normal user has role:

```java
Role.CUSTOMER
```

An admin has role:

```java
Role.ADMIN
```

## Polymorphism

Polymorphism means one parent type can refer to different child objects.

In this project:

```java
Account savingsAccount = Account.createAccount(AccountType.SAVINGS, ...);
Account currentAccount = Account.createAccount(AccountType.CURRENT, ...);
```

Both variables are declared as `Account`.

But the actual objects are different:

```text
savingsAccount -> SavingsAccount object
currentAccount -> CurrentAccount object
```

This lets service code work with the general type:

```java
deposit(Account account, BigDecimal amount)
```

The service does not need to know whether the account is savings or current.

That is polymorphism.

## Abstraction

Abstraction means hiding unnecessary details and exposing only what matters.

`Account.java` is abstract:

```java
public abstract class Account
```

You cannot directly create:

```java
new Account(...)
```

That is because a generic account is incomplete. The real account must be a specific type:

```java
SavingsAccount
CurrentAccount
```

`Account` defines shared behavior:

```java
getBalance()
updateBalance()
createAccount()
```

It also requires child classes to define:

```java
public abstract AccountType getAccountType();
public abstract BigDecimal calculateMaintenanceFee();
```

So every account type must say:

* What type of account it is
* How its maintenance fee is calculated

## Interfaces

An interface defines what operations must exist, but not how they are implemented.

In `BankService.java`:

```java
public interface BankService {
    Transaction deposit(Account account, BigDecimal amount);

    Transaction withdraw(Account account, BigDecimal amount);

    Transaction transferMoney(Account sourceAccount, Account destinationAccount, BigDecimal amount);

    List<Transaction> getTransactionHistory(Account account);
}
```

This says:

```text
Any bank service must provide these methods.
```

`SimpleBankService` implements the interface:

```java
public class SimpleBankService implements BankService
```

So it must provide real code for every interface method.

Why interfaces matter:

Today we have:

```java
SimpleBankService
```

Later we could create:

```java
DatabaseBankService
SecureBankService
AuditBankService
```

All can implement `BankService`, and the rest of the app can depend on the interface.

## User Flow

The user flow is:

```java
User customer = new User("Nishant Sharma", "nishant@example.com", "Pass@123");
customer.registerUser();
customer.loginUser("nishant@example.com", "Pass@123");
```

What happens:

1. A user object is created.
2. `registerUser()` checks that name, email, and password are present.
3. If valid, `registered` becomes `true`.
4. `loginUser()` checks that the user is registered.
5. It compares the provided email and password with the stored values.
6. If they match, `loggedIn` becomes `true`.

Logout:

```java
customer.logoutUser();
```

This sets:

```java
loggedIn = false;
```

## Admin Flow

Admin is created like this:

```java
Admin admin = new Admin("Bank Admin", "admin@securebank.com", "Admin@123", "Operations");
```

The `Admin` constructor calls:

```java
super(fullName, email, password, Role.ADMIN);
```

So the admin gets the same user fields, but with the role set to `ADMIN`.

Admin overrides dashboard behavior:

```java
admin.getDashboardMessage()
```

returns:

```text
Admin dashboard for Bank Admin (Operations)
```

## Account Flow

Account creation uses a static factory method:

```java
Account savingsAccount = Account.createAccount(
        AccountType.SAVINGS,
        "SBP1001",
        customer,
        new BigDecimal("1000.00")
);
```

Inside `createAccount`, Java checks the account type:

```java
if (accountType == AccountType.SAVINGS) {
    return new SavingsAccount(accountNumber, owner, openingBalance);
}
return new CurrentAccount(accountNumber, owner, openingBalance);
```

So the caller asks for an `Account`, but the factory returns the correct subclass.

Then:

```java
savingsAccount.createAccount();
```

sets:

```java
active = true;
```

## Banking Flow

The app uses the interface type:

```java
BankService bankService = new SimpleBankService();
```

This is good design because the application depends on the contract, not the concrete class.

Deposit:

```java
bankService.deposit(savingsAccount, new BigDecimal("250.00"));
```

Inside `SimpleBankService`:

1. Validate amount.
2. Add amount to balance.
3. Create a deposit transaction.
4. Store the transaction in memory.
5. Return the transaction.

Withdraw:

```java
bankService.withdraw(currentAccount, new BigDecimal("100.00"));
```

Inside `SimpleBankService`:

1. Validate amount.
2. Convert amount to negative using `amount.negate()`.
3. Update balance.
4. Create withdrawal transaction.
5. Store transaction.

Transfer:

```java
bankService.transferMoney(savingsAccount, currentAccount, new BigDecimal("300.00"));
```

Inside `SimpleBankService`:

1. Validate amount.
2. Subtract amount from source account.
3. Add amount to destination account.
4. Create transfer transaction.
5. Store transaction.

## Transaction Flow

Transactions are created through:

```java
Transaction.createTransaction(...)
```

This is another static factory method.

Each transaction stores:

```java
transactionId
referenceNumber
transactionType
sourceAccount
destinationAccount
amount
createdAt
```

Transaction history is found by filtering:

```java
Transaction.getTransactionHistory(transactions, account)
```

The method returns transactions where the account is either:

```java
sourceAccount
```

or:

```java
destinationAccount
```

That means transfers appear in both accounts' histories.

## Why BigDecimal Is Used for Money

The account balance and transaction amount use:

```java
BigDecimal
```

Not:

```java
double
```

Money needs precision. Floating point types like `double` can create small rounding errors.

Example problem:

```java
0.1 + 0.2
```

may not be exactly:

```java
0.3
```

`BigDecimal` is better for banking amounts.

## Important Design Decisions

### User and Admin

`Admin extends User` because admin is a special kind of user.

This teaches:

* Inheritance
* Constructor chaining
* Method overriding
* Role-based hierarchy

### Account, SavingsAccount, and CurrentAccount

`Account` is abstract because a generic account should not exist directly.

This teaches:

* Abstraction
* Polymorphism
* Abstract methods
* Shared parent behavior

### BankService and SimpleBankService

`BankService` is an interface because banking operations should be defined separately from implementation details.

This teaches:

* Interfaces
* Implementation classes
* Programming to an interface
* Separation of responsibility

### Transaction

`Transaction` records banking activity.

This teaches:

* Object creation
* Static factory methods
* Immutable fields with `final`
* Filtering lists
* Relationship between objects

## End-to-End Example

This is the complete Phase 1 flow:

```java
User customer = new User("Nishant Sharma", "nishant@example.com", "Pass@123");
customer.registerUser();
customer.loginUser("nishant@example.com", "Pass@123");

Account savingsAccount = Account.createAccount(
        AccountType.SAVINGS,
        "SBP1001",
        customer,
        new BigDecimal("1000.00")
);
savingsAccount.createAccount();

Account currentAccount = Account.createAccount(
        AccountType.CURRENT,
        "SBP2001",
        customer,
        new BigDecimal("500.00")
);
currentAccount.createAccount();

BankService bankService = new SimpleBankService();
bankService.deposit(savingsAccount, new BigDecimal("250.00"));
bankService.withdraw(currentAccount, new BigDecimal("100.00"));
bankService.transferMoney(savingsAccount, currentAccount, new BigDecimal("300.00"));
```

Balances after this:

```text
Savings: 1000 + 250 - 300 = 950
Current: 500 - 100 + 300 = 700
```

## Phase 1 Concepts Checklist

| Concept | Where It Appears |
| --- | --- |
| Java project structure | `src/main/java/com/securebank/pro` |
| Packages | `package com.securebank.pro.entity;` |
| Classes | `User`, `Admin`, `Account`, `Transaction` |
| Objects | `new User(...)`, `new SimpleBankService()` |
| Constructors | `User(...)`, `Admin(...)`, `Account(...)` |
| Access modifiers | `public`, `private`, `protected` |
| Static keyword | `nextUserId`, `createAccount()` |
| Final keyword | `userId`, `accountNumber`, `hasRole()` |
| Methods | `loginUser()`, `deposit()`, `withdraw()` |
| Method overloading | `registerUser()`, `registerUser(...)` |
| Method overriding | `Admin#getDashboardMessage()` |
| Encapsulation | private fields with public getters/methods |
| Inheritance | `Admin extends User` |
| Polymorphism | `Account savingsAccount = new SavingsAccount(...)` |
| Abstraction | `abstract class Account` |
| Interfaces | `BankService` |

## What To Understand Before Phase 2

Before moving ahead, make sure these ideas feel clear:

1. Why fields are private.
2. Why constructors initialize object state.
3. Why `Admin` can reuse `User` behavior.
4. Why `Account` is abstract.
5. Why `SavingsAccount` and `CurrentAccount` override methods.
6. Why `BankService` is an interface.
7. Why `SimpleBankService` contains banking operations.
8. Why transactions are separate objects instead of just print statements.

Once these are comfortable, Phase 2 can move toward stronger validation, exceptions, collections, and service-layer design.
