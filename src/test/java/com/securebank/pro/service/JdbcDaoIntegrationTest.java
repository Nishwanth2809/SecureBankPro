package com.securebank.pro.service;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.securebank.pro.dao.AccountDAO;
import com.securebank.pro.dao.TransactionDAO;
import com.securebank.pro.dao.UserDAO;
import com.securebank.pro.entity.Account;
import com.securebank.pro.entity.Transaction;
import com.securebank.pro.entity.User;
import com.securebank.pro.enums.AccountType;
import com.securebank.pro.enums.TransactionType;
import com.securebank.pro.exception.TransactionFailedException;
import com.securebank.pro.util.DatabaseConnectionManager;

@SpringBootTest
public class JdbcDaoIntegrationTest {

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private AccountDAO accountDAO;

    @Autowired
    private TransactionDAO transactionDAO;

    @Autowired
    private BankService bankService;

    @BeforeEach
    public void setUp() {
        // Initialize schema for the custom pool test
        DatabaseConnectionManager.initializeDatabase();

        // Clear tables via direct SQL execution to make sure H2 has a fresh state
        try (Connection conn = DatabaseConnectionManager.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM transactions");
            stmt.execute("DELETE FROM accounts");
            stmt.execute("DELETE FROM users");
        } catch (SQLException e) {
            fail("Failed to clean up test database: " + e.getMessage());
        }
    }

    @Test
    public void testConnectionPoolingBasics() {
        // Lease all available connections in the custom pool (size 5)
        Connection[] connections = new Connection[5];
        try {
            for (int i = 0; i < 5; i++) {
                connections[i] = DatabaseConnectionManager.getConnection();
                assertNotNull(connections[i]);
            }

            // If we close/recycle one, we can lease it again!
            connections[0].close(); // Proxied close returns it to the pool
            
            Connection newConn = DatabaseConnectionManager.getConnection();
            assertNotNull(newConn);
            newConn.close();

            // Return all other connections to the pool
            for (int i = 1; i < 5; i++) {
                connections[i].close();
            }
        } catch (SQLException e) {
            fail("SQLException in connection pool test: " + e.getMessage());
        }
    }

    @Test
    public void testUserDaoOperations() {
        User user = new User("Jane Smith", "jane.smith@example.com", "Jane@12345");
        user.registerUser();
        userDAO.saveUser(user);

        // Find by email
        User found = userDAO.findUserByEmail("jane.smith@example.com");
        assertNotNull(found);
        assertEquals("Jane Smith", found.getFullName());

        // Update user name and verify
        User updatedUser = new User(user.getUserId(), "Jane Doe", "jane.smith@example.com", "Jane@12345", user.getRole(), true, true);
        userDAO.updateUser(updatedUser);

        User foundUpdated = userDAO.findUserByEmail("jane.smith@example.com");
        assertNotNull(foundUpdated);
        assertEquals("Jane Doe", foundUpdated.getFullName());
        assertTrue(foundUpdated.isLoggedIn());
    }

    @Test
    public void testAccountDaoOperations() {
        User owner = new User("Alice Green", "alice@example.com", "Alice@123");
        owner.registerUser();
        userDAO.saveUser(owner);

        Account account = Account.createAccount(AccountType.SAVINGS, "SBP-SAV-800", owner, new BigDecimal("1000.00"));
        account.createAccount();
        accountDAO.createAccount(account);

        // Fetch
        Account fetched = accountDAO.fetchAccount("SBP-SAV-800");
        assertNotNull(fetched);
        assertEquals(new BigDecimal("1000.00"), fetched.getBalance());
        assertEquals("Alice Green", fetched.getOwner().getFullName());

        // Update balance
        accountDAO.updateBalance("SBP-SAV-800", new BigDecimal("1200.00"));
        Account fetchedUpdated = accountDAO.fetchAccount("SBP-SAV-800");
        assertEquals(new BigDecimal("1200.00"), fetchedUpdated.getBalance());
    }

    @Test
    public void testTransactionDaoAndUserQuery() {
        User owner = new User("Bob Blue", "bob@example.com", "Bob@123456");
        owner.registerUser();
        userDAO.saveUser(owner);

        Account account = Account.createAccount(AccountType.SAVINGS, "SBP-SAV-900", owner, new BigDecimal("500.00"));
        account.createAccount();
        accountDAO.createAccount(account);

        Transaction tx = Transaction.createTransaction(TransactionType.DEPOSIT, account, null, new BigDecimal("100.00"));
        transactionDAO.saveTransaction(tx);

        List<Transaction> list = transactionDAO.getTransactionsByUser(owner.getUserId());
        assertEquals(1, list.size());
        assertEquals("SBP-SAV-900", list.get(0).getSourceAccount().getAccountNumber());
    }

    @Test
    public void testSuccessfulTransactionCommit() {
        User u1 = new User("Sender", "sender@example.com", "Sender@123");
        User u2 = new User("Receiver", "receiver@example.com", "Receiver@123");
        u1.registerUser();
        u2.registerUser();
        userDAO.saveUser(u1);
        userDAO.saveUser(u2);

        Account acc1 = Account.createAccount(AccountType.SAVINGS, "SBP-SND-01", u1, new BigDecimal("500.00"));
        Account acc2 = Account.createAccount(AccountType.CURRENT, "SBP-RCV-01", u2, new BigDecimal("100.00"));
        acc1.createAccount();
        acc2.createAccount();
        accountDAO.createAccount(acc1);
        accountDAO.createAccount(acc2);

        // Perform money transfer (should succeed and commit)
        try {
            bankService.transferMoney(acc1, acc2, new BigDecimal("200.00"));
        } catch (TransactionFailedException e) {
            fail("Transfer failed unexpectedly: " + e.getMessage());
        }

        // Verify balances in database are updated
        Account dbAcc1 = accountDAO.fetchAccount("SBP-SND-01");
        Account dbAcc2 = accountDAO.fetchAccount("SBP-RCV-01");
        assertEquals(new BigDecimal("300.00"), dbAcc1.getBalance());
        assertEquals(new BigDecimal("300.00"), dbAcc2.getBalance());

        // Verify transaction is saved
        List<Transaction> txs = transactionDAO.getTransactionsByUser(u1.getUserId());
        assertEquals(1, txs.size());
    }

    @Test
    public void testFailedTransactionRollback() {
        User u1 = new User("Sender", "sender@example.com", "Sender@123");
        User u2 = new User("Receiver", "receiver@example.com", "Receiver@123");
        u1.registerUser();
        u2.registerUser();
        userDAO.saveUser(u1);
        userDAO.saveUser(u2);

        Account acc1 = Account.createAccount(AccountType.SAVINGS, "SBP-SND-02", u1, new BigDecimal("100.00"));
        Account acc2 = Account.createAccount(AccountType.CURRENT, "SBP-RCV-02", u2, new BigDecimal("100.00"));
        acc1.createAccount();
        acc2.createAccount();
        accountDAO.createAccount(acc1);
        accountDAO.createAccount(acc2);

        // Try transfer with amount exceeding balance (100.00)
        // This will fail with InsufficientBalanceException during updating of acc1
        assertThrows(TransactionFailedException.class, () -> {
            bankService.transferMoney(acc1, acc2, new BigDecimal("300.00"));
        });

        // Verify balances in database remain unchanged (rolled back!)
        Account dbAcc1 = accountDAO.fetchAccount("SBP-SND-02");
        Account dbAcc2 = accountDAO.fetchAccount("SBP-RCV-02");
        assertEquals(new BigDecimal("100.00"), dbAcc1.getBalance());
        assertEquals(new BigDecimal("100.00"), dbAcc2.getBalance());

        // Verify no transactions were stored in the database for the failed transfer
        List<Transaction> txs = transactionDAO.getTransactionsByUser(u1.getUserId());
        assertTrue(txs.isEmpty());
    }
}
