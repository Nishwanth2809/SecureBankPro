-- Drop tables in correct order if they exist
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS accounts;
DROP TABLE IF EXISTS users;

-- Users table (stores customers and admins)
CREATE TABLE users (
    userId INT PRIMARY KEY,
    fullName VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) DEFAULT 'CUSTOMER',
    registered BOOLEAN DEFAULT FALSE,
    loggedIn BOOLEAN DEFAULT FALSE,
    department VARCHAR(50)
);

-- Accounts table (relationship: One user -> many accounts)
CREATE TABLE accounts (
    accountId INT PRIMARY KEY,
    accountNumber VARCHAR(20) NOT NULL UNIQUE,
    ownerId INT NOT NULL,
    balance DECIMAL(15, 2) DEFAULT 0.00,
    active BOOLEAN DEFAULT FALSE,
    accountType VARCHAR(20) NOT NULL,
    FOREIGN KEY (ownerId) REFERENCES users(userId) ON DELETE CASCADE
);

-- Transactions table (relationship: One account -> many transactions)
CREATE TABLE transactions (
    transactionId INT PRIMARY KEY,
    referenceNumber VARCHAR(20) NOT NULL UNIQUE,
    transactionType VARCHAR(20) NOT NULL,
    sourceAccountId INT,
    destinationAccountId INT,
    amount DECIMAL(15, 2) NOT NULL,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sourceAccountId) REFERENCES accounts(accountId) ON DELETE SET NULL,
    FOREIGN KEY (destinationAccountId) REFERENCES accounts(accountId) ON DELETE SET NULL
);
