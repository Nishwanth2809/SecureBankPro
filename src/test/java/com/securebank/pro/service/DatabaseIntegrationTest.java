package com.securebank.pro.service;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.securebank.pro.entity.Account;
import com.securebank.pro.entity.Admin;
import com.securebank.pro.entity.Transaction;
import com.securebank.pro.entity.User;
import com.securebank.pro.enums.AccountType;
import com.securebank.pro.enums.Role;
import com.securebank.pro.enums.TransactionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.securebank.pro.repository.UserRepository;
import com.securebank.pro.repository.AccountRepository;
import com.securebank.pro.repository.TransactionRepository;
import com.securebank.pro.repository.AdminRepository;

@SpringBootTest
public class DatabaseIntegrationTest {

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
        // Clear tables in reverse dependency order
        transactionRepository.clear();
        accountRepository.clear();
        userRepository.clear();
        adminRepository.clear();
    }

    @Test
    public void testSchemaInitialization() {
        // Schema is initialized in setUp, verify we can retrieve all lists without errors
        assertNotNull(userRepository.findAll());
        assertNotNull(accountRepository.findAll());
        assertNotNull(transactionRepository.findAll());
        assertNotNull(adminRepository.findAll());
    }

    @Test
    public void testUserCrudOperations() {
        // Insert user
        User user = new User("John Doe", "john@example.com", "John@123");
        user.registerUser();
        userRepository.save(user);

        // Find by ID
        User foundById = userRepository.findById(user.getUserId());
        assertNotNull(foundById);
        assertEquals("John Doe", foundById.getFullName());

        // Find by email
        User foundByEmail = userRepository.findByEmail("john@example.com");
        assertNotNull(foundByEmail);
        assertEquals(user.getUserId(), foundByEmail.getUserId());

        // Update user property (by modifying name and saving)
        User updatedUser = new User(user.getUserId(), "John Smith", "john@example.com", "John@123", Role.CUSTOMER, true, true);
        userRepository.save(updatedUser);

        User verifiedUser = userRepository.findById(user.getUserId());
        assertEquals("John Smith", verifiedUser.getFullName());
        assertTrue(verifiedUser.isLoggedIn());

        // Delete user
        userRepository.deleteById(user.getUserId());
        assertNull(userRepository.findById(user.getUserId()));
    }

    @Test
    public void testOneToManyUserToAccountsRelationship() {
        // Create owner
        User owner = new User("Alice Green", "alice@example.com", "Alice@123");
        owner.registerUser();
        userRepository.save(owner);

        // Create accounts
        Account savings = Account.createAccount(AccountType.SAVINGS, "SBP-SAV-01", owner, new BigDecimal("1500.00"));
        Account current = Account.createAccount(AccountType.CURRENT, "SBP-CUR-01", owner, new BigDecimal("800.00"));
        savings.createAccount();
        current.createAccount();

        accountRepository.save(savings);
        accountRepository.save(current);

        // Find accounts by ownerId
        List<Account> ownerAccounts = accountRepository.findByOwnerId(owner.getUserId());
        assertEquals(2, ownerAccounts.size());

        // Verify account details
        Account retrievedSavings = accountRepository.findByAccountNumber("SBP-SAV-01");
        assertNotNull(retrievedSavings);
        assertEquals(new BigDecimal("1500.00"), retrievedSavings.getBalance());
        assertEquals(owner.getUserId(), retrievedSavings.getOwner().getUserId());

        // Cascade delete check: deleting user should delete their accounts if Cascade ON DELETE is enabled
        userRepository.deleteById(owner.getUserId());
        assertNull(accountRepository.findByAccountNumber("SBP-SAV-01"));
        assertNull(accountRepository.findByAccountNumber("SBP-CUR-01"));
    }

    @Test
    public void testOneToManyAccountToTransactionsRelationship() {
        // Create owner
        User owner = new User("Bob Blue", "bob@example.com", "Bob@12345");
        owner.registerUser();
        userRepository.save(owner);

        // Create account
        Account account = Account.createAccount(AccountType.SAVINGS, "SBP-SAV-02", owner, new BigDecimal("1000.00"));
        account.createAccount();
        accountRepository.save(account);

        // Save transactions
        Transaction depositTx = Transaction.createTransaction(TransactionType.DEPOSIT, account, null, new BigDecimal("200.00"));
        Transaction withdrawTx = Transaction.createTransaction(TransactionType.WITHDRAWAL, account, null, new BigDecimal("100.00"));
        transactionRepository.save(depositTx);
        transactionRepository.save(withdrawTx);

        // Retrieve transactions by account
        List<Transaction> txHistory = transactionRepository.findByAccount(account);
        assertEquals(2, txHistory.size());

        // Join query verification: verify that findByAccount retrieves the correct account details
        Transaction firstTx = txHistory.get(0);
        assertNotNull(firstTx.getSourceAccount());
        assertEquals(account.getAccountNumber(), firstTx.getSourceAccount().getAccountNumber());
    }

    @Test
    public void testSqlJoinAndWildcardSearch() {
        // Populate multiple users
        User user1 = new User("Jane Patel", "jane.patel@example.com", "Jane@123");
        User user2 = new User("Kiran Patel", "kiran.p@example.com", "Kiran@123");
        User user3 = new User("John Doe", "john.doe@example.com", "John@123");

        user1.registerUser();
        user2.registerUser();
        user3.registerUser();

        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);

        // Perform search using wildcard query
        List<User> searchResults = userRepository.search("Patel");
        assertEquals(2, searchResults.size());

        List<User> searchResultsEmpty = userRepository.search("NotPresent");
        assertTrue(searchResultsEmpty.isEmpty());
    }

    @Autowired
    private com.securebank.pro.service.BankService bankService;

    @Test
    public void testAdminSingleTableInheritance() {
        // Create Admin
        Admin admin = new Admin("System Admin", "admin@securebank.com", "Admin@123", "IT Security");
        admin.registerUser();
        
        // Save using general user repository and admin repository
        userRepository.save(admin);
        adminRepository.save(admin);

        // Query general users: should be present
        User foundUser = userRepository.findByEmail("admin@securebank.com");
        assertNotNull(foundUser);
        assertEquals(Role.ADMIN, foundUser.getRole());

        // Query admin repository: should be retrieved with department detail
        Admin foundAdmin = adminRepository.findByEmail("admin@securebank.com");
        assertNotNull(foundAdmin);
        assertEquals("IT Security", foundAdmin.getDepartment());
    }

    @Test
    public void testProgrammaticTransactionRollbackOnFailure() {
        // Create owner
        User owner = new User("Tx Owner", "tx@example.com", "TxPass@123");
        owner.registerUser();
        userRepository.save(owner);

        // Create accounts
        Account source = Account.createAccount(AccountType.SAVINGS, "SBP-SRC-999", owner, new BigDecimal("1000.00"));
        Account dest = Account.createAccount(AccountType.CURRENT, "SBP-DST-999", owner, new BigDecimal("500.00"));
        source.createAccount();
        dest.createAccount();
        accountRepository.save(source);
        accountRepository.save(dest);

        // Attempt transfer of a negative amount, which will throw IllegalArgumentException/TransactionFailedException
        assertThrows(Exception.class, () -> {
            bankService.processTransfer(source, dest, new BigDecimal("-100.00"));
        });

        // Verify rollback: balances remain unchanged
        Account retSource = accountRepository.findByAccountNumber("SBP-SRC-999");
        Account retDest = accountRepository.findByAccountNumber("SBP-DST-999");
        assertEquals(new BigDecimal("1000.00"), retSource.getBalance());
        assertEquals(new BigDecimal("500.00"), retDest.getBalance());

        // Verify no transactions were persisted
        assertTrue(transactionRepository.findByAccount(source).isEmpty());
    }

    @Test
    public void testConcurrentTransfersThreadSafety() throws Exception {
        // Create owner
        User owner = new User("Concurrent Owner", "concurrent@example.com", "Pass@123");
        owner.registerUser();
        userRepository.save(owner);

        // Create accounts
        Account acc1 = Account.createAccount(AccountType.SAVINGS, "SBP-CON-001", owner, new BigDecimal("1000.00"));
        Account acc2 = Account.createAccount(AccountType.CURRENT, "SBP-CON-002", owner, new BigDecimal("1000.00"));
        acc1.createAccount();
        acc2.createAccount();
        accountRepository.save(acc1);
        accountRepository.save(acc2);

        int numberOfThreads = 10;
        int transfersPerThread = 10;
        BigDecimal amount = new BigDecimal("10.00");

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        // 5 threads transfer from acc1 to acc2
        // 5 threads transfer from acc2 to acc1
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadIdx = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < transfersPerThread; j++) {
                        if (threadIdx % 2 == 0) {
                            bankService.synchronizedTransfer(acc1, acc2, amount);
                        } else {
                            bankService.synchronizedTransfer(acc2, acc1, amount);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Verify balances remain consistent
        Account retAcc1 = accountRepository.findByAccountNumber("SBP-CON-001");
        Account retAcc2 = accountRepository.findByAccountNumber("SBP-CON-002");

        assertEquals(0, new BigDecimal("1000.00").compareTo(retAcc1.getBalance()));
        assertEquals(0, new BigDecimal("1000.00").compareTo(retAcc2.getBalance()));
    }

    @Test
    public void testProcessAsyncTransaction() throws Exception {
        AtomicBoolean executed = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);

        bankService.processAsyncTransaction(() -> {
            executed.set(true);
            latch.countDown();
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertTrue(executed.get());
    }
}
