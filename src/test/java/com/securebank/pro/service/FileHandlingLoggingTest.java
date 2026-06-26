package com.securebank.pro.service;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.securebank.pro.entity.Account;
import com.securebank.pro.entity.Transaction;
import com.securebank.pro.entity.User;
import com.securebank.pro.enums.AccountType;
import com.securebank.pro.repository.UserRepository;
import com.securebank.pro.repository.AccountRepository;
import com.securebank.pro.repository.TransactionRepository;
import com.securebank.pro.repository.AdminRepository;

@SpringBootTest
public class FileHandlingLoggingTest {

    private static final String TEST_TRANSACTIONS_FILE = "logs/transactions.txt";
    private static final String TEST_EXPORT_FILE = "exports/test_statement.txt";
    private static final String TEST_BACKUP_FILE = "backups/test_backup.ser";

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private UserService userService;

    @Autowired
    private BankService bankService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AdminRepository adminRepository;

    @BeforeEach
    public void setUp() {
        // Clear repositories to ensure isolation
        transactionRepository.clear();
        accountRepository.clear();
        userRepository.clear();
        adminRepository.clear();

        // Clean files before running tests
        new File(TEST_TRANSACTIONS_FILE).delete();
        new File(TEST_EXPORT_FILE).delete();
        new File(TEST_BACKUP_FILE).delete();
    }

    @AfterEach
    public void tearDown() {
        // Clean up files created during tests
        new File(TEST_TRANSACTIONS_FILE).delete();
        new File(TEST_EXPORT_FILE).delete();
        new File(TEST_BACKUP_FILE).delete();
    }

    @Test
    public void testSaveAndReadTransactionLogs() {
        User user = new User("Alice Green", "alice@example.com", "Alice@123");
        userService.addUser(user);

        Account account = Account.createAccount(AccountType.SAVINGS, "SBP9001", user, new BigDecimal("500.00"));
        account.createAccount();
        accountRepository.save(account);

        // Perform transaction which triggers saving to file
        Transaction tx1 = bankService.deposit(account, new BigDecimal("100.00"));
        
        // Assert transaction log file exists
        File file = new File(TEST_TRANSACTIONS_FILE);
        assertTrue(file.exists());

        // Read log from file using service
        List<String> logs = fileStorageService.readTransactionHistory();
        assertFalse(logs.isEmpty());
        assertTrue(logs.get(0).contains("TXN-"));
        assertTrue(logs.get(0).contains("DEPOSIT"));
        assertTrue(logs.get(0).contains("100.00"));
    }

    @Test
    public void testExportStatement() {
        User user = new User("Alice Green", "alice@example.com", "Alice@123");
        userService.addUser(user);

        Account account = Account.createAccount(AccountType.SAVINGS, "SBP9001", user, new BigDecimal("500.00"));
        account.createAccount();
        accountRepository.save(account);

        bankService.deposit(account, new BigDecimal("150.00"));
        bankService.withdraw(account, new BigDecimal("50.00"));

        // Export statement
        fileStorageService.exportStatement(account, TEST_EXPORT_FILE);

        File file = new File(TEST_EXPORT_FILE);
        assertTrue(file.exists());
        assertTrue(file.length() > 0);
    }

    @Test
    public void testDatabaseSerializationBackupAndRestore() {
        User user1 = new User("Alice Green", "alice@example.com", "Alice@123");
        User user2 = new User("Bob Blue", "bob@example.com", "Bob@12345");
        userService.addUser(user1);
        userService.addUser(user2);

        Account account1 = Account.createAccount(AccountType.SAVINGS, "SBP9001", user1, new BigDecimal("500.00"));
        Account account2 = Account.createAccount(AccountType.CURRENT, "SBP9002", user2, new BigDecimal("100.00"));
        account1.createAccount();
        account2.createAccount();
        accountRepository.save(account1);
        accountRepository.save(account2);

        bankService.deposit(account1, new BigDecimal("200.00"));
        try {
            bankService.transferMoney(account1, account2, new BigDecimal("100.00"));
        } catch (Exception e) {
            fail("Transfer failed unexpectedly: " + e.getMessage());
        }

        // Check size before backup
        int usersSizeBefore = userRepository.findAll().size();
        int accountsSizeBefore = accountRepository.findAll().size();
        int transactionsSizeBefore = transactionRepository.findAll().size();

        // Perform Backup
        fileStorageService.backupDatabase(TEST_BACKUP_FILE);
        assertTrue(new File(TEST_BACKUP_FILE).exists());

        // Clear repositories
        userRepository.clear();
        accountRepository.clear();
        transactionRepository.clear();

        assertEquals(0, userRepository.findAll().size());
        assertEquals(0, accountRepository.findAll().size());
        assertEquals(0, transactionRepository.findAll().size());

        // Perform Restore
        fileStorageService.restoreDatabase(TEST_BACKUP_FILE);

        // Verify restoration
        assertEquals(usersSizeBefore, userRepository.findAll().size());
        assertEquals(accountsSizeBefore, accountRepository.findAll().size());
        assertEquals(transactionsSizeBefore, transactionRepository.findAll().size());

        // Verify content details restored correctly
        User recoveredUser = userRepository.findByEmail("alice@example.com");
        assertNotNull(recoveredUser);
        assertEquals("Alice Green", recoveredUser.getFullName());

        Account recoveredAccount = accountRepository.findByAccountNumber("SBP9001");
        assertNotNull(recoveredAccount);
        assertEquals(new BigDecimal("600.00"), recoveredAccount.getBalance()); // 500 + 200 - 100
    }
}
