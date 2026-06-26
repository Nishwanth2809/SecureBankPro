package com.securebank.pro.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.securebank.pro.dto.request.CreateAccountRequestDTO;
import com.securebank.pro.dto.request.DepositRequestDTO;
import com.securebank.pro.dto.request.LoginRequestDTO;
import com.securebank.pro.dto.request.RegisterRequestDTO;
import com.securebank.pro.dto.request.TransferRequestDTO;
import com.securebank.pro.dto.request.WithdrawRequestDTO;
import com.securebank.pro.entity.Admin;
import com.securebank.pro.entity.User;
import com.securebank.pro.repository.AccountRepository;
import com.securebank.pro.repository.TransactionRepository;
import com.securebank.pro.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
public class RestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setUp() {
        transactionRepository.clear();
        accountRepository.clear();
        userRepository.clear();
    }

    private String obtainAccessToken(String email, String password) throws Exception {
        LoginRequestDTO loginReq = new LoginRequestDTO();
        loginReq.setEmail(email);
        loginReq.setPassword(password);

        String responseBody = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(responseBody).get("token").asText();
    }

    @Test
    public void testAuthWorkflow() throws Exception {
        // 1. Register User
        RegisterRequestDTO regReq = new RegisterRequestDTO();
        regReq.setFullName("John Rest");
        regReq.setEmail("john.rest@example.com");
        regReq.setPassword("Password@123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("registered")));

        // 2. Login User
        LoginRequestDTO loginReq = new LoginRequestDTO();
        loginReq.setEmail("john.rest@example.com");
        loginReq.setPassword("Password@123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.token").exists());

        // 3. Check Session
        mockMvc.perform(get("/api/auth/session")
                .param("email", "john.rest@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true));

        // 4. Logout User
        mockMvc.perform(post("/api/auth/logout")
                .param("email", "john.rest@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 5. Check Session (Inactive)
        mockMvc.perform(get("/api/auth/session")
                .param("email", "john.rest@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(false));
    }

    @Test
    public void testAccountAndBankingWorkflow() throws Exception {
        // 1. Register Owner
        RegisterRequestDTO regReq = new RegisterRequestDTO();
        regReq.setFullName("Alice Bank");
        regReq.setEmail("alice.bank@example.com");
        regReq.setPassword("Alice@123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regReq)))
                .andExpect(status().isOk());

        // Authenticate to obtain token for Alice
        String token = obtainAccessToken("alice.bank@example.com", "Alice@123");

        // 2. Create Account (requires authentication)
        CreateAccountRequestDTO createAccReq = new CreateAccountRequestDTO();
        createAccReq.setAccountNumber("SBP-REST-101");
        createAccReq.setOwnerEmail("alice.bank@example.com");
        createAccReq.setAccountType("SAVINGS");
        createAccReq.setBalance(new BigDecimal("1000.00"));

        mockMvc.perform(post("/api/accounts/create")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createAccReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("SBP-REST-101"))
                .andExpect(jsonPath("$.balance").value(1000.00))
                .andExpect(jsonPath("$.accountType").value("SAVINGS"));

        // 3. Get Account details (requires authentication)
        mockMvc.perform(get("/api/accounts/SBP-REST-101")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("SBP-REST-101"))
                .andExpect(jsonPath("$.balance").value(1000.00));

        // 4. Get all accounts (requires authentication)
        mockMvc.perform(get("/api/accounts")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].accountNumber").value("SBP-REST-101"));

        // 5. Deposit Money (requires authentication)
        DepositRequestDTO depReq = new DepositRequestDTO();
        depReq.setAccountNumber("SBP-REST-101");
        depReq.setAmount(new BigDecimal("200.00"));

        mockMvc.perform(post("/api/transactions/deposit")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(depReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(200.00))
                .andExpect(jsonPath("$.transactionType").value("DEPOSIT"));

        // 6. Withdraw Money (requires authentication)
        WithdrawRequestDTO wReq = new WithdrawRequestDTO();
        wReq.setAccountNumber("SBP-REST-101");
        wReq.setAmount(new BigDecimal("100.00"));

        mockMvc.perform(post("/api/transactions/withdraw")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(wReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.transactionType").value("WITHDRAWAL"));

        // 7. Get Transaction History (requires authentication)
        mockMvc.perform(get("/api/transactions/history/SBP-REST-101")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    public void testAdminActionsWorkflow() throws Exception {
        // Create Admin user in DB
        Admin admin = new Admin(0, "System Admin", "admin@example.com", passwordEncoder.encode("AdminPassword@123"), com.securebank.pro.enums.Role.ADMIN, true, false, "Operations");
        userRepository.save(admin);

        // Obtain tokens
        String adminToken = obtainAccessToken("admin@example.com", "AdminPassword@123");

        // 1. Register Owner (Bob)
        RegisterRequestDTO regReq = new RegisterRequestDTO();
        regReq.setFullName("Bob AdminTest");
        regReq.setEmail("bob.admin@example.com");
        regReq.setPassword("BobPassword@123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regReq)))
                .andExpect(status().isOk());

        String bobToken = obtainAccessToken("bob.admin@example.com", "BobPassword@123");

        // Create Account for Bob (requires bob's token or admin's token)
        CreateAccountRequestDTO createAccReq = new CreateAccountRequestDTO();
        createAccReq.setAccountNumber("SBP-ADMIN-99");
        createAccReq.setOwnerEmail("bob.admin@example.com");
        createAccReq.setAccountType("CURRENT");
        createAccReq.setBalance(new BigDecimal("500.00"));

        mockMvc.perform(post("/api/accounts/create")
                .header("Authorization", "Bearer " + bobToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createAccReq)))
                .andExpect(status().isOk());

        // 2. Freeze Account (requires ADMIN token)
        mockMvc.perform(put("/api/admin/accounts/SBP-ADMIN-99/freeze")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 3. Verify Account is Inactive / Frozen (transaction fails)
        DepositRequestDTO depReq = new DepositRequestDTO();
        depReq.setAccountNumber("SBP-ADMIN-99");
        depReq.setAmount(new BigDecimal("100.00"));

        mockMvc.perform(post("/api/transactions/deposit")
                .header("Authorization", "Bearer " + bobToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(depReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("not active")));

        // 4. Unblock Account (requires ADMIN token)
        mockMvc.perform(put("/api/admin/accounts/SBP-ADMIN-99/unblock")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 5. Verify Account is Active again (transaction succeeds)
        mockMvc.perform(post("/api/transactions/deposit")
                .header("Authorization", "Bearer " + bobToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(depReq)))
                .andExpect(status().isOk());

        // 6. Delete User (requires ADMIN token)
        int userId = userRepository.findByEmail("bob.admin@example.com").getUserId();
        mockMvc.perform(delete("/api/admin/users/" + userId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 7. Verify User & Account deleted (cascade constraint)
        assertNull(userRepository.findById(userId));
        assertNull(accountRepository.findByAccountNumber("SBP-ADMIN-99"));
    }

    @Test
    public void testValidationAndGlobalExceptionsWorkflow() throws Exception {
        // 1. Invalid Register DTO (empty fields, bad email, short password)
        RegisterRequestDTO badReg = new RegisterRequestDTO();
        badReg.setFullName("");
        badReg.setEmail("invalid-email");
        badReg.setPassword("123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(badReg)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.fullName").exists())
                .andExpect(jsonPath("$.errors.email").value("Email format is invalid"))
                .andExpect(jsonPath("$.errors.password").value("Password must be at least 6 characters"));

        // 2. Invalid Deposit DTO (empty account number, null amount)
        DepositRequestDTO badDep = new DepositRequestDTO();
        badDep.setAccountNumber("");
        badDep.setAmount(null);

        // Register a user to obtain valid JWT to hit protected transactions
        RegisterRequestDTO regReq = new RegisterRequestDTO();
        regReq.setFullName("Validator Test");
        regReq.setEmail("validator@example.com");
        regReq.setPassword("Password@123");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regReq)))
                .andExpect(status().isOk());

        String token = obtainAccessToken("validator@example.com", "Password@123");

        mockMvc.perform(post("/api/transactions/deposit")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(badDep)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.accountNumber").value("Account number is required"))
                .andExpect(jsonPath("$.errors.amount").value("Amount is required"));

        // 3. Test Invalid Account details (returns 404 Not Found)
        mockMvc.perform(get("/api/accounts/NON-EXISTENT-ACCOUNT")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}
