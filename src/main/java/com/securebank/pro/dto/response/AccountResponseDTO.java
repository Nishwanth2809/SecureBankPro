package com.securebank.pro.dto.response;

import java.math.BigDecimal;

public class AccountResponseDTO {
    private String accountNumber;
    private BigDecimal balance;
    private String accountType;
    private String ownerName;

    public AccountResponseDTO() {}

    public AccountResponseDTO(String accountNumber, BigDecimal balance, String accountType, String ownerName) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.accountType = accountType;
        this.ownerName = ownerName;
    }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
}
