package com.securebank.pro.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class CreateAccountRequestDTO {
    @NotBlank(message = "Owner email is required")
    @Email(message = "Email format is invalid")
    private String ownerEmail;

    @NotBlank(message = "Account number is required")
    @Size(min = 10, max = 20, message = "Account number must be between 10 and 20 characters")
    private String accountNumber;

    @NotNull(message = "Initial balance is required")
    private BigDecimal balance;

    @NotBlank(message = "Account type is required")
    private String accountType; // SAVINGS or CURRENT

    public CreateAccountRequestDTO() {}

    public CreateAccountRequestDTO(String ownerEmail, String accountNumber, BigDecimal balance, String accountType) {
        this.ownerEmail = ownerEmail;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.accountType = accountType;
    }

    public String getOwnerEmail() { return ownerEmail; }
    public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
}
