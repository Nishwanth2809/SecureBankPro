package com.securebank.pro.entity;

import java.math.BigDecimal;
import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;

import com.securebank.pro.enums.AccountType;

@Entity
@DiscriminatorValue("SAVINGS")
public class SavingsAccount extends Account {
    private static final long serialVersionUID = 1L;
    private static final BigDecimal MAINTENANCE_FEE = BigDecimal.ZERO;

    protected SavingsAccount() {
        super();
    }

    public SavingsAccount(String accountNumber, User owner, BigDecimal openingBalance) {
        super(accountNumber, owner, openingBalance);
    }

    public SavingsAccount(int accountId, String accountNumber, User owner, BigDecimal balance, boolean active) {
        super(accountId, accountNumber, owner, balance, active);
    }

    @Override
    public AccountType getAccountType() {
        return AccountType.SAVINGS;
    }

    @Override
    public BigDecimal calculateMaintenanceFee() {
        return MAINTENANCE_FEE;
    }
}
