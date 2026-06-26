package com.securebank.pro.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.securebank.pro.dao.AccountDAO;
import com.securebank.pro.dao.TransactionDAO;
import com.securebank.pro.entity.Account;
import com.securebank.pro.entity.Transaction;
import com.securebank.pro.enums.TransactionType;
import com.securebank.pro.exception.InsufficientBalanceException;
import com.securebank.pro.exception.InvalidAccountException;
import com.securebank.pro.exception.TransactionFailedException;
import com.securebank.pro.service.BankService;
import com.securebank.pro.service.FileStorageService;
import com.securebank.pro.validation.AccountValidator;
import com.securebank.pro.validation.TransactionValidator;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class SimpleBankService implements BankService {

    private final TransactionDAO transactionDAO;
    private final AccountDAO accountDAO;
    private final FileStorageService fileStorageService;
    private final PlatformTransactionManager transactionManager;
    private final ExecutorService executorService;

    public SimpleBankService(TransactionDAO transactionDAO, AccountDAO accountDAO, FileStorageService fileStorageService, PlatformTransactionManager transactionManager) {
        this.transactionDAO = transactionDAO;
        this.accountDAO = accountDAO;
        this.fileStorageService = fileStorageService;
        this.transactionManager = transactionManager;
        this.executorService = Executors.newFixedThreadPool(10);
    }

    @PreDestroy
    public void shutdownExecutor() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    /**
     * Deposits an amount into an account.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Transaction deposit(Account account, BigDecimal amount) {
        AccountValidator.validateAccountStatus(account);
        TransactionValidator.validateTransferAmount(amount);

        account.updateBalance(amount);
        accountDAO.updateBalance(account.getAccountNumber(), account.getBalance());

        Transaction transaction = Transaction.createTransaction(
            TransactionType.DEPOSIT,
            account,
            null,
            amount
        );
        transactionDAO.saveTransaction(transaction);
        fileStorageService.saveTransactionLog(transaction);
        com.securebank.pro.util.BankLogger.info("Deposited " + amount + " USD into account '" + account.getAccountNumber() + "'. New balance: " + account.getBalance() + " USD.");
        return transaction;
    }

    /**
     * Withdraws an amount from an account.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Transaction withdraw(Account account, BigDecimal amount) {
        AccountValidator.validateAccountStatus(account);
        TransactionValidator.validateTransferAmount(amount);

        account.updateBalance(amount.negate());
        accountDAO.updateBalance(account.getAccountNumber(), account.getBalance());

        Transaction transaction = Transaction.createTransaction(
            TransactionType.WITHDRAWAL,
            account,
            null,
            amount
        );
        transactionDAO.saveTransaction(transaction);
        fileStorageService.saveTransactionLog(transaction);
        com.securebank.pro.util.BankLogger.info("Withdrew " + amount + " USD from account '" + account.getAccountNumber() + "'. New balance: " + account.getBalance() + " USD.");
        return transaction;
    }

    /**
     * Transfers money from the source account to the destination account.
     * Delegates to processTransfer for programmatic transaction management.
     */
    @Override
    public Transaction transferMoney(Account sourceAccount, Account destinationAccount, BigDecimal amount)
            throws TransactionFailedException {
        return processTransfer(sourceAccount, destinationAccount, amount);
    }

    /**
     * Programmatic transaction handling for transfer operations.
     */
    @Override
    public Transaction processTransfer(Account sourceAccount, Account destinationAccount, BigDecimal amount)
            throws TransactionFailedException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("TransferTransaction");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            AccountValidator.validateAccountStatus(sourceAccount);
            AccountValidator.validateAccountStatus(destinationAccount);
            TransactionValidator.validateTransferAmount(amount);

            // Debit sender
            sourceAccount.updateBalance(amount.negate());
            accountDAO.updateBalance(sourceAccount.getAccountNumber(), sourceAccount.getBalance());

            // Credit receiver
            destinationAccount.updateBalance(amount);
            accountDAO.updateBalance(destinationAccount.getAccountNumber(), destinationAccount.getBalance());

            Transaction transaction = Transaction.createTransaction(
                TransactionType.TRANSFER,
                sourceAccount,
                destinationAccount,
                amount
            );
            transactionDAO.saveTransaction(transaction);
            fileStorageService.saveTransactionLog(transaction);

            // Commit transaction
            transactionManager.commit(status);

            com.securebank.pro.util.BankLogger.info("Transferred " + amount + " USD from account '" + sourceAccount.getAccountNumber() + "' to account '" + destinationAccount.getAccountNumber() + "'.");
            return transaction;

        } catch (Exception e) {
            rollbackTransaction(status);
            String errorMsg = "Transfer of " + amount + " USD from '" + sourceAccount.getAccountNumber()
                + "' to '" + destinationAccount.getAccountNumber() + "' failed: " + e.getMessage();
            com.securebank.pro.util.BankLogger.severe(errorMsg, e);
            throw new TransactionFailedException(errorMsg, e);
        }
    }

    /**
     * Explicitly rolls back the transaction.
     */
    @Override
    public void rollbackTransaction(TransactionStatus status) {
        if (status != null && !status.isCompleted()) {
            transactionManager.rollback(status);
            com.securebank.pro.util.BankLogger.warn("Transaction rolled back successfully.");
        }
    }

    /**
     * Executes a transfer in a thread-safe, synchronized, and deadlock-free manner.
     */
    @Override
    public Transaction synchronizedTransfer(Account sourceAccount, Account destinationAccount, BigDecimal amount)
            throws TransactionFailedException {
        if (sourceAccount == null || destinationAccount == null) {
            throw new InvalidAccountException("Source and destination accounts must not be null.");
        }

        // Avoid deadlocks by acquiring locks in consistent alphanumerical order of account numbers
        Account firstLock = sourceAccount.getAccountNumber().compareTo(destinationAccount.getAccountNumber()) < 0 ? sourceAccount : destinationAccount;
        Account secondLock = firstLock == sourceAccount ? destinationAccount : sourceAccount;

        synchronized (firstLock) {
            synchronized (secondLock) {
                return transferMoney(sourceAccount, destinationAccount, amount);
            }
        }
    }

    /**
     * Executes a transaction task asynchronously in the background.
     */
    @Override
    public void processAsyncTransaction(Runnable task) {
        if (task != null) {
            executorService.submit(task);
        }
    }

    /**
     * Returns an unmodifiable view of this account's transaction history.
     */
    @Override
    public List<Transaction> getTransactionHistory(Account account) {
        if (account == null) {
            throw new InvalidAccountException("Cannot retrieve history for a null account.");
        }
        if (account.getOwner() == null) {
            return Collections.emptyList();
        }

        List<Transaction> userTx = transactionDAO.getTransactionsByUser(account.getOwner().getUserId());
        List<Transaction> accountTx = new ArrayList<>();
        for (Transaction tx : userTx) {
            if (tx.belongsTo(account)) {
                accountTx.add(tx);
            }
        }
        return Collections.unmodifiableList(accountTx);
    }
}
