package com.securebank.pro.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.securebank.pro.dto.response.ApiResponseDTO;
import com.securebank.pro.service.AccountService;
import com.securebank.pro.service.UserService;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Administrative operations: freeze/unblock accounts and delete users. " +
        "Requires **ROLE_ADMIN** JWT token. Login with admin@securebank.com / Admin@123.")
@SecurityRequirement(name = "BearerAuth")
public class AdminController {

    private final AccountService accountService;
    private final UserService userService;

    public AdminController(AccountService accountService, UserService userService) {
        this.accountService = accountService;
        this.userService = userService;
    }

    // ── FREEZE ACCOUNT ────────────────────────────────────────────────────────

    @Operation(
        summary = "Freeze a bank account",
        description = "Suspends all banking operations (deposit/withdraw/transfer) on the account. " +
                "The account owner can still log in and view their account details. " +
                "Requires ROLE_ADMIN JWT token."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Account frozen successfully",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {"success": true, "message": "Account SBP1001 has been frozen."}
                    """))),
        @ApiResponse(responseCode = "400", description = "Account not found"),
        @ApiResponse(responseCode = "403", description = "Access denied — ROLE_ADMIN required")
    })
    @PutMapping("/accounts/{accountNumber}/freeze")
    public ResponseEntity<ApiResponseDTO> freezeAccount(
            @Parameter(description = "Account number to freeze", example = "SBP1001")
            @PathVariable String accountNumber) {
        try {
            accountService.freezeAccount(accountNumber);
            return ResponseEntity.ok(new ApiResponseDTO(true, "Account " + accountNumber + " has been frozen."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponseDTO(false, e.getMessage()));
        }
    }

    // ── UNBLOCK ACCOUNT ───────────────────────────────────────────────────────

    @Operation(
        summary = "Unblock a frozen account",
        description = "Restores full banking access to a previously frozen account. " +
                "Requires ROLE_ADMIN JWT token."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Account unblocked successfully",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {"success": true, "message": "Account SBP1001 has been unblocked."}
                    """))),
        @ApiResponse(responseCode = "400", description = "Account not found"),
        @ApiResponse(responseCode = "403", description = "Access denied — ROLE_ADMIN required")
    })
    @PutMapping("/accounts/{accountNumber}/unblock")
    public ResponseEntity<ApiResponseDTO> unblockAccount(
            @Parameter(description = "Account number to unblock", example = "SBP1001")
            @PathVariable String accountNumber) {
        try {
            accountService.unblockAccount(accountNumber);
            return ResponseEntity.ok(new ApiResponseDTO(true, "Account " + accountNumber + " has been unblocked."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponseDTO(false, e.getMessage()));
        }
    }

    // ── DELETE USER ───────────────────────────────────────────────────────────

    @Operation(
        summary = "Delete a user by ID",
        description = "Permanently removes a user and all their associated accounts and transactions " +
                "(via ON DELETE CASCADE). This action is **irreversible**. " +
                "Requires ROLE_ADMIN JWT token."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User deleted successfully",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {"success": true, "message": "User with ID 2 was successfully removed"}
                    """))),
        @ApiResponse(responseCode = "400", description = "User not found"),
        @ApiResponse(responseCode = "403", description = "Access denied — ROLE_ADMIN required")
    })
    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponseDTO> deleteUser(
            @Parameter(description = "Numeric user ID to delete", example = "2")
            @PathVariable int id) {
        try {
            userService.removeUser(id);
            return ResponseEntity.ok(new ApiResponseDTO(true, "User with ID " + id + " was successfully removed"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponseDTO(false, e.getMessage()));
        }
    }

    // ── LIST ALL USERS WITH ACCOUNTS ──────────────────────────────────────────
    @GetMapping("/users")
    public ResponseEntity<java.util.List<com.securebank.pro.dto.response.UserResponseDTO>> getAllUsers() {
        java.util.List<com.securebank.pro.entity.User> users = userService.getAllUsers();
        java.util.List<com.securebank.pro.entity.Account> allAccounts = accountService.getAllAccounts();
        
        java.util.Map<Integer, java.util.List<String>> userAccountsMap = new java.util.HashMap<>();
        for (com.securebank.pro.entity.Account acc : allAccounts) {
            if (acc.getOwner() != null) {
                userAccountsMap
                    .computeIfAbsent(acc.getOwner().getUserId(), k -> new java.util.ArrayList<>())
                    .add(acc.getAccountNumber());
            }
        }
        
        java.util.List<com.securebank.pro.dto.response.UserResponseDTO> response = users.stream().map(u -> {
            java.util.List<String> accs = userAccountsMap.getOrDefault(u.getUserId(), java.util.Collections.emptyList());
            return new com.securebank.pro.dto.response.UserResponseDTO(
                u.getUserId(),
                u.getFullName(),
                u.getEmail(),
                u.getRole() != null ? u.getRole().name() : "CUSTOMER",
                accs
            );
        }).collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // ── DELETE ACCOUNT ────────────────────────────────────────────────────────
    @DeleteMapping("/accounts/{accountNumber}")
    public ResponseEntity<ApiResponseDTO> deleteAccount(@PathVariable String accountNumber) {
        try {
            accountService.deleteAccount(accountNumber);
            return ResponseEntity.ok(new ApiResponseDTO(true, "Account " + accountNumber + " has been successfully deleted."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponseDTO(false, e.getMessage()));
        }
    }
}
