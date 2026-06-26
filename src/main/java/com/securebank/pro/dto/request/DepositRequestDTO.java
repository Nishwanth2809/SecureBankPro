package com.securebank.pro.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "Request payload for depositing money into an account")

public class DepositRequestDTO {
    @Schema(description = "Target account number", example = "SBP1001")
    @NotBlank(message = "Account number is required")
    private String accountNumber;

    @Schema(description = "Amount to deposit (must be positive)", example = "500.00")
    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    public DepositRequestDTO() {}

    public DepositRequestDTO(String accountNumber, BigDecimal amount) {
        this.accountNumber = accountNumber;
        this.amount = amount;
    }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
