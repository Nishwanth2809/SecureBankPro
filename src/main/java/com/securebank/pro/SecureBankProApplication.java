package com.securebank.pro;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.securebank.pro.entity.Account;
import com.securebank.pro.entity.Admin;
import com.securebank.pro.entity.Transaction;
import com.securebank.pro.entity.User;
import com.securebank.pro.enums.AccountType;
import com.securebank.pro.exception.TransactionFailedException;
import com.securebank.pro.repository.AccountRepository;
import com.securebank.pro.repository.AdminRepository;
import com.securebank.pro.repository.TransactionRepository;
import com.securebank.pro.repository.UserRepository;
import com.securebank.pro.service.AuthService;
import com.securebank.pro.service.BankService;
import com.securebank.pro.service.FileStorageService;
import com.securebank.pro.service.UserService;
import com.securebank.pro.util.BankLogger;

@SpringBootApplication
public class SecureBankProApplication implements CommandLineRunner {

    private final UserService userService;
    private final AuthService authService;
    private final BankService bankService;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AdminRepository adminRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public SecureBankProApplication(
            UserService userService,
            AuthService authService,
            BankService bankService,
            FileStorageService fileStorageService,
            UserRepository userRepository,
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            AdminRepository adminRepository,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.authService = authService;
        this.bankService = bankService;
        this.fileStorageService = fileStorageService;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public static void main(String[] args) {
        SpringApplication.run(SecureBankProApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // Clear repositories first to ensure clean database state for demo
        userRepository.clear();
        accountRepository.clear();
        transactionRepository.clear();
        adminRepository.clear();

        // Clean up old files from previous runs to ensure fresh logs
        new File("logs/transactions.txt").delete();
        new File("logs/bank_activity.log").delete();
        new File("exports/Nishant_Savings_Statement.txt").delete();
        new File("backups/database.ser").delete();

        BankLogger.info("Starting SecureBankPro Application — Phase 7: Spring Boot Setup");

        System.out.println("============================================================");
        System.out.println("  SecureBankPro — Phase 7: Spring Boot Setup");
        System.out.println("============================================================\n");

        // ── Step 1: Initialize System & Populate Data ─────────────────────────
        System.out.println("──── 1. Populate System Data (Automatically logged via BankLogger) ────");

        User nishant = new User("Nishant Sharma", "nishant@example.com", "Pass@123");
        User kiran = new User("Kiran Patel", "kiran@example.com", "Kiran@567");
        userService.addUser(nishant);
        userService.addUser(kiran);

        Admin admin = new Admin(0, "Bank Admin", "admin@securebank.com", passwordEncoder.encode("Admin@123"), com.securebank.pro.enums.Role.ADMIN, true, false, "Operations");
        userRepository.save(admin);
        adminRepository.save(admin);

        Account savings = Account.createAccount(AccountType.SAVINGS, "SBP1001", nishant, new BigDecimal("1000.00"));
        Account current = Account.createAccount(AccountType.CURRENT, "SBP2001", nishant, new BigDecimal("500.00"));
        savings.createAccount();
        current.createAccount();

        accountRepository.save(savings);
        accountRepository.save(current);

        // Perform some transactions (triggers file writing and logging)
        bankService.deposit(savings, new BigDecimal("250.00"));
        bankService.withdraw(savings, new BigDecimal("100.00"));
        try {
            bankService.transferMoney(savings, current, new BigDecimal("150.00"));
        } catch (TransactionFailedException e) {
            System.out.println("Transfer failed: " + e.getMessage());
        }

        // Test logging in auth events
        authService.login("nishant@example.com", "Pass@123");
        authService.login("kiran@example.com", "WrongPassword");

        // ── Step 2: Read Transaction Logs ──────
        System.out.println("\n──── 2. Read Transaction Logs from logs/transactions.txt ────");
        List<String> rawHistory = fileStorageService.readTransactionHistory();
        System.out.println("Raw transaction log lines read from file:");
        for (String record : rawHistory) {
            System.out.println("  " + record);
        }

        // ── Step 3: Export Statement ────────────
        System.out.println("\n──── 3. Generate Account Statement to exports/Nishant_Savings_Statement.txt ────");
        fileStorageService.exportStatement(savings, "exports/Nishant_Savings_Statement.txt");
        System.out.println("Checking if export statement file exists in exports/: " + new File("exports/Nishant_Savings_Statement.txt").exists());

        // ── Step 4: Serialization Backup & Restore ───────────────────────────
        System.out.println("\n──── 4. Database Backup & Restore via Object Serialization ────");
        System.out.println("Before Backup State:");
        System.out.println("  Registered Users   : " + userRepository.findAll().size());
        System.out.println("  Registered Accounts: " + accountRepository.findAll().size());
        System.out.println("  Log Transactions   : " + transactionRepository.findAll().size());

        // Run Backup
        fileStorageService.backupDatabase("backups/database.ser");

        // Clear all repositories in-memory
        System.out.println("\n[Action] Clearing all database repositories...");
        userRepository.clear();
        accountRepository.clear();
        transactionRepository.clear();
        adminRepository.clear();

        System.out.println("Cleared State (verifying):");
        System.out.println("  Registered Users   : " + userRepository.findAll().size());
        System.out.println("  Registered Accounts: " + accountRepository.findAll().size());
        System.out.println("  Log Transactions   : " + transactionRepository.findAll().size());

        // Run Restore
        System.out.println("\n[Action] Restoring database from backup file backups/database.ser...");
        fileStorageService.restoreDatabase("backups/database.ser");

        System.out.println("Restored State (verifying recovery):");
        System.out.println("  Registered Users   : " + userRepository.findAll().size());
        System.out.println("  Registered Accounts: " + accountRepository.findAll().size());
        System.out.println("  Log Transactions   : " + transactionRepository.findAll().size());

        // Test lookups to ensure identity is preserved
        User recoveredUser = userRepository.findByEmail("nishant@example.com");
        if (recoveredUser != null) {
            System.out.println("  Recovered User Name: " + recoveredUser.getFullName());
        }
        Account recoveredAccount = accountRepository.findByAccountNumber("SBP1001");
        if (recoveredAccount != null) {
            System.out.println("  Recovered Account Owner Balance: " + recoveredAccount.getBalance() + " USD");
        }

        System.out.println("\n============================================================");
        System.out.println("  SecureBankPro — Phase 7 Spring Boot Setup Complete");
        System.out.println("============================================================\n");
    }
}
