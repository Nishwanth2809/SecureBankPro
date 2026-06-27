-- ── SecureBankPro Schema — PostgreSQL / Supabase ─────────────────────────────

-- Users table (stores customers and admins via Single Table Inheritance)
-- All column names are unquoted lowercase so PostgreSQL stores them as lowercase.
-- Hibernate with PhysicalNamingStrategyStandardImpl generates lowercase identifiers
-- that match exactly.
CREATE TABLE IF NOT EXISTS users (
    "userId"     SERIAL PRIMARY KEY,
    "fullName"   VARCHAR(100)  NOT NULL,
    email        VARCHAR(100)  NOT NULL UNIQUE,
    password     VARCHAR(255)  NOT NULL,
    role         VARCHAR(20)   DEFAULT 'CUSTOMER',
    registered   BOOLEAN       DEFAULT FALSE,
    "loggedIn"   BOOLEAN       DEFAULT FALSE,
    department   VARCHAR(50)
);

-- Accounts table (One user -> many accounts)
CREATE TABLE IF NOT EXISTS accounts (
    "accountId"     SERIAL PRIMARY KEY,
    "accountNumber" VARCHAR(20)    NOT NULL UNIQUE,
    "ownerId"       INT            NOT NULL,
    balance         DECIMAL(15, 2) DEFAULT 0.00,
    active          BOOLEAN        DEFAULT FALSE,
    "accountType"   VARCHAR(20)    NOT NULL,
    FOREIGN KEY ("ownerId") REFERENCES users("userId") ON DELETE CASCADE
);

-- Transactions table (One account -> many transactions)
CREATE TABLE IF NOT EXISTS transactions (
    "transactionId"        SERIAL PRIMARY KEY,
    "referenceNumber"      VARCHAR(20)    NOT NULL UNIQUE,
    "transactionType"      VARCHAR(20)    NOT NULL,
    "sourceAccountId"      INT,
    "destinationAccountId" INT,
    amount                 DECIMAL(15, 2) NOT NULL,
    "createdAt"            TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY ("sourceAccountId")      REFERENCES accounts("accountId") ON DELETE SET NULL,
    FOREIGN KEY ("destinationAccountId") REFERENCES accounts("accountId") ON DELETE SET NULL
);
