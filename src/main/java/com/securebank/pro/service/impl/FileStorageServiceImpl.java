package com.securebank.pro.service.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.securebank.pro.dto.DatabaseBackup;
import com.securebank.pro.entity.Account;
import com.securebank.pro.entity.Transaction;
import com.securebank.pro.entity.User;
import com.securebank.pro.repository.AccountRepository;
import com.securebank.pro.repository.TransactionRepository;
import com.securebank.pro.repository.UserRepository;
import com.securebank.pro.service.FileStorageService;
import com.securebank.pro.util.BankLogger;
import org.springframework.stereotype.Service;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final String DEFAULT_LOG_FILE = "logs/transactions.txt";
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public FileStorageServiceImpl(UserRepository userRepository, AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void saveTransactionLog(Transaction transaction) {
        if (transaction == null) return;
        
        File file = new File(DEFAULT_LOG_FILE);
        ensureParentDirectoriesExist(file);

        // Try-with-resources closes the writer automatically
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            String sourceNo = transaction.getSourceAccount() != null ? transaction.getSourceAccount().getAccountNumber() : "";
            String destNo = transaction.getDestinationAccount() != null ? transaction.getDestinationAccount().getAccountNumber() : "";
            
            String line = String.format("%d,%s,%s,%s,%s,%s,%s",
                transaction.getTransactionId(),
                transaction.getReferenceNumber(),
                transaction.getTransactionType().name(),
                sourceNo,
                destNo,
                transaction.getAmount().toString(),
                transaction.getCreatedAt().toString()
            );
            writer.write(line);
            writer.newLine();
            BankLogger.info("Saved transaction log to file: " + transaction.getReferenceNumber());
        } catch (IOException e) {
            BankLogger.severe("Failed to save transaction log: " + transaction.getReferenceNumber(), e);
        }
    }

    @Override
    public List<String> readTransactionHistory() {
        List<String> history = new ArrayList<>();
        File file = new File(DEFAULT_LOG_FILE);
        if (!file.exists()) {
            return history;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                history.add(line);
            }
            BankLogger.info("Successfully read transaction history file.");
        } catch (IOException e) {
            BankLogger.severe("Failed to read transaction history file.", e);
        }
        return history;
    }

    @Override
    public void exportStatement(Account account, String destFilePath) {
        if (account == null || destFilePath == null) {
            throw new IllegalArgumentException("Account and file path are required.");
        }

        File file = new File(destFilePath);
        ensureParentDirectoriesExist(file);

        List<Transaction> transactions = transactionRepository.findByAccount(account);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("=========================================================================");
            writer.newLine();
            writer.write("                    SECUREBANKPRO - ACCOUNT STATEMENT                    ");
            writer.newLine();
            writer.write("=========================================================================");
            writer.newLine();
            writer.write("Statement Generated On: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            writer.newLine();
            writer.write("Account Number        : " + account.getAccountNumber());
            writer.newLine();
            writer.write("Account Type          : " + account.getAccountType().name());
            writer.newLine();
            writer.write("Owner Name            : " + (account.getOwner() != null ? account.getOwner().getFullName() : "N/A"));
            writer.newLine();
            writer.write("Current Balance       : " + account.getBalance() + " USD");
            writer.newLine();
            writer.write("-------------------------------------------------------------------------");
            writer.newLine();
            writer.write(String.format("%-10s | %-12s | %-12s | %-12s | %-20s", "Tx ID", "Reference", "Type", "Amount", "Timestamp"));
            writer.newLine();
            writer.write("-------------------------------------------------------------------------");
            writer.newLine();

            for (Transaction tx : transactions) {
                writer.write(String.format("%-10d | %-12s | %-12s | %-12s | %-20s",
                    tx.getTransactionId(),
                    tx.getReferenceNumber(),
                    tx.getTransactionType().name(),
                    tx.getAmount().toString(),
                    tx.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                ));
                writer.newLine();
            }

            writer.write("=========================================================================");
            writer.newLine();
            writer.write("                    Thank you for banking with us!                     ");
            writer.newLine();
            writer.write("=========================================================================");
            writer.newLine();

            BankLogger.info("Successfully exported statement for account " + account.getAccountNumber() + " to " + destFilePath);
        } catch (IOException e) {
            BankLogger.severe("Failed to export account statement to " + destFilePath, e);
        }
    }

    @Override
    public void backupDatabase(String backupFilePath) {
        if (backupFilePath == null) {
            throw new IllegalArgumentException("Backup file path is required.");
        }

        File file = new File(backupFilePath);
        ensureParentDirectoriesExist(file);

        List<User> users = userRepository.findAll();
        List<Account> accounts = accountRepository.findAll();
        List<Transaction> transactions = transactionRepository.findAll();

        DatabaseBackup backup = new DatabaseBackup(users, accounts, transactions);

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(backup);
            BankLogger.info("Database backup completed successfully to: " + backupFilePath);
        } catch (IOException e) {
            BankLogger.severe("Database backup failed: " + backupFilePath, e);
        }
    }

    @Override
    public void restoreDatabase(String backupFilePath) {
        if (backupFilePath == null) {
            throw new IllegalArgumentException("Backup file path is required.");
        }

        File file = new File(backupFilePath);
        if (!file.exists()) {
            BankLogger.warn("Restore failed: backup file does not exist: " + backupFilePath);
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            DatabaseBackup backup = (DatabaseBackup) ois.readObject();

            // Clear repositories
            userRepository.clear();
            accountRepository.clear();
            transactionRepository.clear();

            // Restore users and track mapping
            java.util.Map<Integer, User> userMap = new java.util.HashMap<>();
            for (User user : backup.getUsers()) {
                int oldId = user.getUserId();
                setField(user, "userId", 0);
                
                // Replace the accounts collection with a new empty list to avoid lazy proxy loading exceptions
                setField(user, "accounts", new java.util.ArrayList<>());
                
                userRepository.save(user);
                userMap.put(oldId, user);
            }

            // Restore accounts and track mapping
            java.util.Map<Integer, Account> accountMap = new java.util.HashMap<>();
            for (Account account : backup.getAccounts()) {
                int oldId = account.getAccountId();
                if (account.getOwner() != null) {
                    User newOwner = userMap.get(account.getOwner().getUserId());
                    if (newOwner != null) {
                        setField(account, "owner", newOwner);
                    }
                }
                setField(account, "accountId", 0);
                accountRepository.save(account);
                accountMap.put(oldId, account);
            }

            // Restore transactions
            for (Transaction transaction : backup.getTransactions()) {
                setField(transaction, "transactionId", 0);
                if (transaction.getSourceAccount() != null) {
                    Account newSrc = accountMap.get(transaction.getSourceAccount().getAccountId());
                    if (newSrc != null) {
                        setField(transaction, "sourceAccount", newSrc);
                    }
                }
                if (transaction.getDestinationAccount() != null) {
                    Account newDest = accountMap.get(transaction.getDestinationAccount().getAccountId());
                    if (newDest != null) {
                        setField(transaction, "destinationAccount", newDest);
                    }
                }
                transactionRepository.save(transaction);
            }

            BankLogger.info("Database restore completed successfully from: " + backupFilePath);
        } catch (IOException | ClassNotFoundException e) {
            BankLogger.severe("Database restore failed: " + backupFilePath, e);
        }
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = getDeclaredFieldRecursive(target.getClass(), fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field via reflection: " + fieldName, e);
        }
    }

    private Object getField(Object target, String fieldName) {
        try {
            java.lang.reflect.Field field = getDeclaredFieldRecursive(target.getClass(), fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get field via reflection: " + fieldName, e);
        }
    }

    private java.lang.reflect.Field getDeclaredFieldRecursive(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            if (clazz.getSuperclass() != null) {
                return getDeclaredFieldRecursive(clazz.getSuperclass(), fieldName);
            }
            throw e;
        }
    }

    private void ensureParentDirectoriesExist(File file) {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
    }
}
