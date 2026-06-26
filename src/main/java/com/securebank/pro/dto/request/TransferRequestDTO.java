package com.securebank.pro.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "Request payload for transferring money between two accounts")

public class TransferRequestDTO {
    @Schema(description = "Account number to debit", example = "SBP1001")
    @NotBlank(message = "Source account number is required")
    private String sourceAccountNumber;

    @Schema(description = "Account number to credit", example = "SBP2001")
    @NotBlank(message = "Destination account number is required")
    private String destinationAccountNumber;

    @Schema(description = "Amount to transfer (must be positive)", example = "150.00")
    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    public TransferRequestDTO() {}

    public TransferRequestDTO(String sourceAccountNumber, String destinationAccountNumber, BigDecimal amount) {
        this.sourceAccountNumber = sourceAccountNumber;
        this.destinationAccountNumber = destinationAccountNumber;
        this.amount = amount;
    }

    public String getSourceAccountNumber() { return sourceAccountNumber; }
    public void setSourceAccountNumber(String sourceAccountNumber) { this.sourceAccountNumber = sourceAccountNumber; }

    public String getDestinationAccountNumber() { return destinationAccountNumber; }
    public void setDestinationAccountNumber(String destinationAccountNumber) { this.destinationAccountNumber = destinationAccountNumber; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
