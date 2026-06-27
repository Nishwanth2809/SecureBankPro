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
        BankLogger.info("Starting SecureBankPro Application");

        System.out.println("============================================================");
        System.out.println("  SecureBankPro Application started");
        System.out.println("============================================================\n");

        // Seed Admin User only if not already present
        if (userRepository.findByEmail("admin@securebank.com") == null) {
            Admin admin = new Admin(0, "Bank Admin", "admin@securebank.com", passwordEncoder.encode("Admin@123"), com.securebank.pro.enums.Role.ADMIN, true, false, "Operations");
            userRepository.save(admin);
            adminRepository.save(admin);
            System.out.println("  Admin User Seeded: admin@securebank.com / Admin@123");
        } else {
            System.out.println("  Admin User already exists. Skipping seeding.");
        }

        System.out.println("  Registered Users   : " + userRepository.findAll().size());
        System.out.println("  Registered Accounts: " + accountRepository.findAll().size());
        System.out.println("============================================================\n");
    }
}
