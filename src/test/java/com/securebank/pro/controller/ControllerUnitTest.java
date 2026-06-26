package com.securebank.pro.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.securebank.pro.dto.mapper.DtoMapper;
import com.securebank.pro.dto.request.DepositRequestDTO;
import com.securebank.pro.dto.request.LoginRequestDTO;
import com.securebank.pro.dto.request.TransferRequestDTO;
import com.securebank.pro.dto.request.WithdrawRequestDTO;
import com.securebank.pro.dto.response.TransactionResponseDTO;
import com.securebank.pro.entity.Account;
import com.securebank.pro.entity.Transaction;
import com.securebank.pro.service.AccountService;
import com.securebank.pro.service.AuthService;
import com.securebank.pro.service.BankService;
import com.securebank.pro.service.UserService;

@WebMvcTest(controllers = {AuthController.class, TransactionController.class})
@AutoConfigureMockMvc(addFilters = false) // Bypass security filters for isolated controller testing
public class ControllerUnitTest {

    static {
        System.setProperty("net.bytebuddy.experimental", "true");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserService userService;

    @MockBean
    private BankService bankService;

    @MockBean
    private AccountService accountService;

    @MockBean
    private DtoMapper dtoMapper;

    @MockBean
    private com.securebank.pro.service.FileStorageService fileStorageService;

    @MockBean
    private com.securebank.pro.repository.UserRepository userRepository;

    @MockBean
    private com.securebank.pro.repository.AccountRepository accountRepository;

    @MockBean
    private com.securebank.pro.repository.TransactionRepository transactionRepository;

    @MockBean
    private com.securebank.pro.repository.AdminRepository adminRepository;

    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @MockBean
    private com.securebank.pro.util.JwtTokenUtil jwtTokenUtil;

    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @Test
    public void shouldLoginSuccessfully() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("user@example.com", "Pass@123");
        
        when(authService.authenticateUser("user@example.com", "Pass@123")).thenReturn("mocked-jwt-token");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.token").value("mocked-jwt-token"));
    }

    @Test
    public void shouldTransferMoney() throws Exception {
        TransferRequestDTO request = new TransferRequestDTO("SBP1001", "SBP2001", new BigDecimal("100.00"));
        
        Account src = mock(Account.class);
        Account dest = mock(Account.class);
        Transaction tx = mock(Transaction.class);
        TransactionResponseDTO responseDTO = new TransactionResponseDTO("TX-REF-123", "TRANSFER", new BigDecimal("100.00"), java.time.LocalDateTime.now());

        when(accountService.fetchAccount("SBP1001")).thenReturn(src);
        when(accountService.fetchAccount("SBP2001")).thenReturn(dest);
        when(bankService.transferMoney(src, dest, new BigDecimal("100.00"))).thenReturn(tx);
        when(dtoMapper.convertEntityToDTO(tx)).thenReturn(responseDTO);

        mockMvc.perform(post("/api/transactions/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.referenceNumber").value("TX-REF-123"))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.transactionType").value("TRANSFER"));
    }

    @Test
    public void shouldRejectInvalidTransfer() throws Exception {
        TransferRequestDTO request = new TransferRequestDTO("SBP1001", "SBP2001", new BigDecimal("100.00"));

        when(accountService.fetchAccount("SBP1001")).thenReturn(null);

        mockMvc.perform(post("/api/transactions/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Source or Destination account not found."));
    }

    @Test
    public void shouldDepositSuccessfully() throws Exception {
        DepositRequestDTO request = new DepositRequestDTO("SBP1001", new BigDecimal("50.00"));
        Account acc = mock(Account.class);
        Transaction tx = mock(Transaction.class);
        TransactionResponseDTO responseDTO = new TransactionResponseDTO("TX-REF-123", "DEPOSIT", new BigDecimal("50.00"), java.time.LocalDateTime.now());

        when(accountService.fetchAccount("SBP1001")).thenReturn(acc);
        when(bankService.deposit(acc, new BigDecimal("50.00"))).thenReturn(tx);
        when(dtoMapper.convertEntityToDTO(tx)).thenReturn(responseDTO);

        mockMvc.perform(post("/api/transactions/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.referenceNumber").value("TX-REF-123"))
                .andExpect(jsonPath("$.amount").value(50.00))
                .andExpect(jsonPath("$.transactionType").value("DEPOSIT"));
    }

    @Test
    public void shouldWithdrawSuccessfully() throws Exception {
        WithdrawRequestDTO request = new WithdrawRequestDTO("SBP1001", new BigDecimal("50.00"));
        Account acc = mock(Account.class);
        Transaction tx = mock(Transaction.class);
        TransactionResponseDTO responseDTO = new TransactionResponseDTO("TX-REF-123", "WITHDRAWAL", new BigDecimal("50.00"), java.time.LocalDateTime.now());

        when(accountService.fetchAccount("SBP1001")).thenReturn(acc);
        when(bankService.withdraw(acc, new BigDecimal("50.00"))).thenReturn(tx);
        when(dtoMapper.convertEntityToDTO(tx)).thenReturn(responseDTO);

        mockMvc.perform(post("/api/transactions/withdraw")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.referenceNumber").value("TX-REF-123"))
                .andExpect(jsonPath("$.amount").value(50.00))
                .andExpect(jsonPath("$.transactionType").value("WITHDRAWAL"));
    }
}
