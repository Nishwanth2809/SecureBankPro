package com.securebank.pro.service;

import java.math.BigDecimal;
import java.util.List;

import com.securebank.pro.entity.Account;
import com.securebank.pro.entity.Transaction;
import com.securebank.pro.exception.TransactionFailedException;

public interface BankService {
    Transaction deposit(Account account, BigDecimal amount);

    Transaction withdraw(Account account, BigDecimal amount);

    /**
     * Transfers money from one account to another.
     *
     * Phase 2 change: now declares "throws TransactionFailedException".
     * This is a CHECKED exception, so every implementation and every caller
     * must explicitly handle or re-declare it.
     *
     * This forces callers to acknowledge that a transfer can fail and plan
     * for it — unlike unchecked exceptions which can be silently ignored.
     */
    Transaction transferMoney(Account sourceAccount, Account destinationAccount, BigDecimal amount)
            throws TransactionFailedException;

    Transaction processTransfer(Account sourceAccount, Account destinationAccount, BigDecimal amount)
            throws TransactionFailedException;

    void rollbackTransaction(org.springframework.transaction.TransactionStatus status);

    Transaction synchronizedTransfer(Account sourceAccount, Account destinationAccount, BigDecimal amount)
            throws TransactionFailedException;

    void processAsyncTransaction(Runnable task);

    List<Transaction> getTransactionHistory(Account account);
}
