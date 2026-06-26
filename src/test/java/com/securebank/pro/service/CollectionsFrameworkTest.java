package com.securebank.pro.service;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.securebank.pro.entity.Account;
import com.securebank.pro.entity.Transaction;
import com.securebank.pro.entity.User;
import com.securebank.pro.enums.AccountType;
import com.securebank.pro.enums.TransactionType;
import com.securebank.pro.exception.UserNotFoundException;
import com.securebank.pro.repository.UserRepository;
import com.securebank.pro.repository.AccountRepository;
import com.securebank.pro.repository.TransactionRepository;
import com.securebank.pro.repository.AdminRepository;

@SpringBootTest
public class CollectionsFrameworkTest {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @Autowired
    private TransactionService transactionService;

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
        // Clear repositories to ensure test isolation
        transactionRepository.clear();
        accountRepository.clear();
        userRepository.clear();
        adminRepository.clear();
    }

    @Test
    public void testUserStorageAndLookup() {
        User user1 = new User("Alice Green", "alice@example.com", "Alice@123");
        User user2 = new User("Bob Blue", "bob@example.com", "Bob@12345");

        // Test addUser
        userService.addUser(user1);
        userService.addUser(user2);

        // Test lookup
        User retrieved = userService.getUserByEmail("alice@example.com");
        assertNotNull(retrieved);
        assertEquals("Alice Green", retrieved.getFullName());

        // Test duplicate email exception
        User dupUser = new User("Alice Copy", "alice@example.com", "Copy@1234");
        assertThrows(IllegalArgumentException.class, () -> userService.addUser(dupUser));

        // Test lookup by ID
        User byId = userService.getUserById(user1.getUserId());
        assertEquals(user1, byId);
    }

    @Test
    public void testUserSearchWithIterator() {
        User user1 = new User("John Doe", "john@example.com", "John@123");
        User user2 = new User("Jane Smith", "jane@example.com", "Jane@123");
        User user3 = new User("Kiran Doe", "kiran@example.com", "Kiran@123");

        userService.addUser(user1);
        userService.addUser(user2);
        userService.addUser(user3);

        // Search name
        List<User> results = userService.searchUser("doe");
        assertEquals(2, results.size()); // John Doe and Kiran Doe

        // Search email
        List<User> emailResults = userService.searchUser("smith");
        assertEquals(1, emailResults.size()); // Jane Smith
    }

    @Test
    public void testRemoveUser() {
        User user = new User("Test User", "test@example.com", "Test@123");
        userService.addUser(user);

        assertNotNull(userService.getUserById(user.getUserId()));

        userService.removeUser(user.getUserId());
        assertNull(userService.getUserById(user.getUserId()));

        // Removing non-existent user should throw UserNotFoundException
        assertThrows(UserNotFoundException.class, () -> userService.removeUser(9999));
    }

    @Test
    public void testActiveSessionsAndActivityLog() {
        User user = new User("Alice Green", "alice@example.com", "Alice@123");
        userService.addUser(user);

        // Before login
        assertFalse(authService.isSessionActive("alice@example.com"));

        // Login
        boolean success = authService.login("alice@example.com", "Alice@123");
        assertTrue(success);
        assertTrue(authService.isSessionActive("alice@example.com"));

        // Logout
        authService.logout("alice@example.com");
        assertFalse(authService.isSessionActive("alice@example.com"));

        // Activity log size limit and ordering (FIFO)
        for (int i = 0; i < 12; i++) {
            authService.login("alice@example.com", "WrongPassword");
        }

        Queue<String> log = authService.getSessionActivityLog();
        // Queue size should be capped at 10
        assertTrue(log.size() <= 10);
    }

    @Test
    public void testTransactionSortingAndFiltering() {
        User user = new User("Alice Green", "alice@example.com", "Alice@123");
        userService.addUser(user);

        Account account1 = Account.createAccount(AccountType.SAVINGS, "SBP1001", user, new BigDecimal("1000.00"));
        account1.createAccount();
        accountRepository.save(account1);

        Transaction tx1 = bankService.deposit(account1, new BigDecimal("100.00")); // ID 1, Amount 100
        Transaction tx2 = bankService.withdraw(account1, new BigDecimal("50.00"));  // ID 2, Amount 50
        Transaction tx3 = bankService.deposit(account1, new BigDecimal("200.00")); // ID 3, Amount 200

        List<Transaction> list = transactionService.getAllTransactions();
        assertEquals(3, list.size());

        // Natural sort by ID ascending
        List<Transaction> naturalSorted = transactionService.sortTransactionsNatural(list);
        assertTrue(naturalSorted.get(0).getTransactionId() < naturalSorted.get(1).getTransactionId());
        assertTrue(naturalSorted.get(1).getTransactionId() < naturalSorted.get(2).getTransactionId());

        // Custom sort by amount descending
        List<Transaction> amountSorted = transactionService.sortTransactions(list, Transaction.BY_AMOUNT_DESC);
        assertEquals(new BigDecimal("200.00"), amountSorted.get(0).getAmount());
        assertEquals(new BigDecimal("100.00"), amountSorted.get(1).getAmount());
        assertEquals(new BigDecimal("50.00"), amountSorted.get(2).getAmount());

        // Custom sort by amount ascending
        List<Transaction> amountSortedAsc = transactionService.sortTransactions(list, Transaction.BY_AMOUNT_ASC);
        assertEquals(new BigDecimal("50.00"), amountSortedAsc.get(0).getAmount());

        // Filtering
        List<Transaction> deposits = transactionService.filterTransactions(list, TransactionType.DEPOSIT);
        assertEquals(2, deposits.size());
        
        List<Transaction> withdrawals = transactionService.filterTransactions(list, TransactionType.WITHDRAWAL);
        assertEquals(1, withdrawals.size());
    }

    @Test
    public void testTransactionQueue() {
        User user = new User("Alice Green", "alice@example.com", "Alice@123");
        userService.addUser(user);

        Account account1 = Account.createAccount(AccountType.SAVINGS, "SBP1001", user, new BigDecimal("1000.00"));
        account1.createAccount();
        accountRepository.save(account1);

        Transaction tx1 = bankService.deposit(account1, new BigDecimal("100.00"));
        Transaction tx2 = bankService.withdraw(account1, new BigDecimal("50.00"));

        // Queue
        transactionRepository.queuePending(tx1);
        transactionRepository.queuePending(tx2);

        assertEquals(2, transactionRepository.getPendingQueue().size());
        assertEquals(tx1, transactionRepository.peekPending());

        // Poll
        Transaction pulled = transactionRepository.pollPending();
        assertEquals(tx1, pulled);
        assertEquals(1, transactionRepository.getPendingQueue().size());

        Transaction pulled2 = transactionRepository.pollPending();
        assertEquals(tx2, pulled2);
        assertEquals(0, transactionRepository.getPendingQueue().size());
    }
}
