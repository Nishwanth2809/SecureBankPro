package com.securebank.pro.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponseDTO {
    private String referenceNumber;
    private String transactionType;
    private BigDecimal amount;
    private LocalDateTime createdAt;

    public TransactionResponseDTO() {}

    public TransactionResponseDTO(String referenceNumber, String transactionType, BigDecimal amount, LocalDateTime createdAt) {
        this.referenceNumber = referenceNumber;
        this.transactionType = transactionType;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
