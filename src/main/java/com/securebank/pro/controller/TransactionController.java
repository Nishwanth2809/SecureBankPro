package com.securebank.pro.controller;

import java.util.List;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.securebank.pro.dto.mapper.DtoMapper;
import com.securebank.pro.dto.request.DepositRequestDTO;
import com.securebank.pro.dto.request.TransferRequestDTO;
import com.securebank.pro.dto.request.WithdrawRequestDTO;
import com.securebank.pro.dto.response.ApiResponseDTO;
import com.securebank.pro.dto.response.TransactionResponseDTO;
import com.securebank.pro.entity.Account;
import com.securebank.pro.entity.Transaction;
import com.securebank.pro.service.AccountService;
import com.securebank.pro.service.BankService;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transactions", description = "Deposit, withdraw, transfer money, and retrieve transaction history. " +
        "All operations require JWT authentication and operate on real account balances.")
@SecurityRequirement(name = "BearerAuth")
public class TransactionController {

    private final BankService bankService;
    private final AccountService accountService;
    private final DtoMapper dtoMapper;

    public TransactionController(BankService bankService, AccountService accountService, DtoMapper dtoMapper) {
        this.bankService = bankService;
        this.accountService = accountService;
        this.dtoMapper = dtoMapper;
    }

    // ── DEPOSIT ───────────────────────────────────────────────────────────────

    @Operation(
        summary = "Deposit money into an account",
        description = "Credits the specified amount to the account balance. " +
                "Returns the transaction record including a unique reference number."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Deposit successful",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = TransactionResponseDTO.class),
                examples = @ExampleObject(value = """
                    {
                      "transactionId": 4,
                      "referenceNumber": "TXN-A1B2C3D4",
                      "transactionType": "DEPOSIT",
                      "sourceAccount": null,
                      "destinationAccount": "SBP1001",
                      "amount": 500.00,
                      "createdAt": "2026-06-26T12:00:00"
                    }
                    """))),
        @ApiResponse(responseCode = "400", description = "Account not found or account frozen",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {"success": false, "message": "Account not found: SBP9999"}
                    """)))
    })
    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(
            @RequestBody(
                description = "Deposit details",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DepositRequestDTO.class),
                    examples = @ExampleObject(
                        name = "Deposit ₹500 to SBP1001",
                        value = """
                            {"accountNumber": "SBP1001", "amount": 500.00}
                            """
                    )
                )
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody DepositRequestDTO request) {
        try {
            Account account = accountService.fetchAccount(request.getAccountNumber());
            if (account == null) {
                return ResponseEntity.badRequest().body(new ApiResponseDTO(false, "Account not found: " + request.getAccountNumber()));
            }
            Transaction tx = bankService.deposit(account, request.getAmount());
            return ResponseEntity.ok(dtoMapper.convertEntityToDTO(tx));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponseDTO(false, e.getMessage()));
        }
    }

    // ── WITHDRAW ──────────────────────────────────────────────────────────────

    @Operation(
        summary = "Withdraw money from an account",
        description = "Debits the specified amount from the account. " +
                "Fails if the account has insufficient balance or is frozen. " +
                "The transaction is rolled back automatically on any failure."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Withdrawal successful",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = TransactionResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Insufficient balance, account frozen, or account not found")
    })
    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(
            @RequestBody(
                description = "Withdrawal details",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = WithdrawRequestDTO.class),
                    examples = @ExampleObject(
                        name = "Withdraw ₹100 from SBP1001",
                        value = """
                            {"accountNumber": "SBP1001", "amount": 100.00}
                            """
                    )
                )
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody WithdrawRequestDTO request) {
        try {
            Account account = accountService.fetchAccount(request.getAccountNumber());
            if (account == null) {
                return ResponseEntity.badRequest().body(new ApiResponseDTO(false, "Account not found: " + request.getAccountNumber()));
            }
            Transaction tx = bankService.withdraw(account, request.getAmount());
            return ResponseEntity.ok(dtoMapper.convertEntityToDTO(tx));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponseDTO(false, e.getMessage()));
        }
    }

    // ── TRANSFER ──────────────────────────────────────────────────────────────

    @Operation(
        summary = "Transfer money between accounts",
        description = "Atomically debits the source account and credits the destination account. " +
                "Uses programmatic transaction management — if any step fails, the entire transfer is rolled back. " +
                "Thread-safe via synchronized locking ordered by account number."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transfer successful",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = TransactionResponseDTO.class),
                examples = @ExampleObject(value = """
                    {
                      "referenceNumber": "TXN-FF001122",
                      "transactionType": "TRANSFER",
                      "sourceAccount": "SBP1001",
                      "destinationAccount": "SBP2001",
                      "amount": 150.00
                    }
                    """))),
        @ApiResponse(responseCode = "400", description = "Source/destination not found, insufficient balance, or account frozen")
    })
    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(
            @RequestBody(
                description = "Transfer details",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TransferRequestDTO.class),
                    examples = @ExampleObject(
                        name = "Transfer ₹150 from SBP1001 to SBP2001",
                        value = """
                            {
                              "sourceAccountNumber": "SBP1001",
                              "destinationAccountNumber": "SBP2001",
                              "amount": 150.00
                            }
                            """
                    )
                )
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody TransferRequestDTO request) {
        try {
            Account source = accountService.fetchAccount(request.getSourceAccountNumber());
            Account dest = accountService.fetchAccount(request.getDestinationAccountNumber());
            if (source == null || dest == null) {
                return ResponseEntity.badRequest().body(new ApiResponseDTO(false, "Source or Destination account not found."));
            }
            Transaction tx = bankService.transferMoney(source, dest, request.getAmount());
            return ResponseEntity.ok(dtoMapper.convertEntityToDTO(tx));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponseDTO(false, e.getMessage()));
        }
    }

    // ── TRANSACTION HISTORY ───────────────────────────────────────────────────

    @Operation(
        summary = "Get transaction history for an account",
        description = "Returns all transactions (deposits, withdrawals, and transfers) associated with the given account number."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transaction history returned",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransactionResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Account not found")
    })
    @GetMapping("/history/{accountNumber}")
    public ResponseEntity<?> getHistory(
            @Parameter(description = "Account number to query history for", example = "SBP1001")
            @PathVariable String accountNumber) {
        Account account = accountService.fetchAccount(accountNumber);
        if (account == null) {
            return ResponseEntity.badRequest().body(new ApiResponseDTO(false, "Account not found: " + accountNumber));
        }
        List<Transaction> history = bankService.getTransactionHistory(account);
        List<TransactionResponseDTO> response = history.stream()
            .map(dtoMapper::convertEntityToDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
