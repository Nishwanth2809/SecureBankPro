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
import com.securebank.pro.dto.request.CreateAccountRequestDTO;
import com.securebank.pro.dto.response.AccountResponseDTO;
import com.securebank.pro.dto.response.ApiResponseDTO;
import com.securebank.pro.entity.Account;
import com.securebank.pro.entity.User;
import com.securebank.pro.enums.AccountType;
import com.securebank.pro.service.AccountService;
import com.securebank.pro.service.UserService;

@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Accounts", description = "Create and retrieve bank accounts. Requires a valid JWT token.")
@SecurityRequirement(name = "BearerAuth")
public class AccountController {

    private final AccountService accountService;
    private final UserService userService;
    private final DtoMapper dtoMapper;

    public AccountController(AccountService accountService, UserService userService, DtoMapper dtoMapper) {
        this.accountService = accountService;
        this.userService = userService;
        this.dtoMapper = dtoMapper;
    }

    // ── CREATE ACCOUNT ────────────────────────────────────────────────────────

    @Operation(
        summary = "Create a new bank account",
        description = "Creates a SAVINGS or CURRENT account linked to an existing user by email. " +
                "The account is activated immediately upon creation."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Account created successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = AccountResponseDTO.class),
                examples = @ExampleObject(value = """
                    {
                      "accountNumber": "SBP3001",
                      "accountType": "SAVINGS",
                      "ownerName": "John Doe",
                      "ownerEmail": "john@example.com",
                      "balance": 500.00,
                      "active": true
                    }
                    """))),
        @ApiResponse(responseCode = "400", description = "Owner not found or validation error",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {"success": false, "message": "Owner user not found with email: unknown@example.com"}
                    """)))
    })
    @PostMapping("/create")
    public ResponseEntity<?> createAccount(
            @RequestBody(
                description = "Account creation details",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CreateAccountRequestDTO.class),
                    examples = @ExampleObject(
                        name = "Sample Request",
                        value = """
                            {
                              "ownerEmail": "nishant@example.com",
                              "accountNumber": "SBP3001",
                              "accountType": "SAVINGS",
                              "balance": 500.00
                            }
                            """
                    )
                )
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody CreateAccountRequestDTO request) {
        try {
            User owner = userService.getUserByEmail(request.getOwnerEmail());
            if (owner == null) {
                return ResponseEntity.badRequest().body(new ApiResponseDTO(false, "Owner user not found with email " + request.getOwnerEmail()));
            }
            AccountType type = AccountType.valueOf(request.getAccountType().toUpperCase());
            Account account = Account.createAccount(type, request.getAccountNumber(), owner, request.getBalance());
            account.createAccount();
            accountService.createAccount(account);
            return ResponseEntity.ok(dtoMapper.convertEntityToDTO(account));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponseDTO(false, e.getMessage()));
        }
    }

    // ── FETCH SINGLE ACCOUNT ──────────────────────────────────────────────────

    @Operation(
        summary = "Get account by account number",
        description = "Returns the details of a single account. Pre-seeded accounts: SBP1001 (Savings), SBP2001 (Current)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Account found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccountResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @GetMapping("/{accountNumber}")
    public ResponseEntity<?> fetchAccount(
            @Parameter(description = "Account number to look up", example = "SBP1001")
            @PathVariable String accountNumber) {
        Account account = accountService.fetchAccount(accountNumber);
        if (account == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dtoMapper.convertEntityToDTO(account));
    }

    // ── GET ALL ACCOUNTS ──────────────────────────────────────────────────────

    @Operation(
        summary = "Get all accounts",
        description = "Returns a list of all bank accounts in the system."
    )
    @ApiResponse(responseCode = "200", description = "List of all accounts",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccountResponseDTO.class)))
    @GetMapping
    public ResponseEntity<List<AccountResponseDTO>> getAllAccounts() {
        List<Account> accounts = accountService.getAllAccounts();
        List<AccountResponseDTO> response = accounts.stream()
            .map(dtoMapper::convertEntityToDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
