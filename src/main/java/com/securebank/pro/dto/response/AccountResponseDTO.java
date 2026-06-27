package com.securebank.pro.dto.response;

import java.math.BigDecimal;

public class AccountResponseDTO {
    private String accountNumber;
    private BigDecimal balance;
    private String accountType;
    private String ownerName;
    private String ownerEmail;

    public AccountResponseDTO() {}

    public AccountResponseDTO(String accountNumber, BigDecimal balance, String accountType, String ownerName, String ownerEmail) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.accountType = accountType;
        this.ownerName = ownerName;
        this.ownerEmail = ownerEmail;
    }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getOwnerEmail() { return ownerEmail; }
    public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }
}
