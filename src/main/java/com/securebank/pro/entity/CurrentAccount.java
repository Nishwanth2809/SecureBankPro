package com.securebank.pro.entity;

import java.math.BigDecimal;
import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;

import com.securebank.pro.enums.AccountType;

@Entity
@DiscriminatorValue("CURRENT")
public class CurrentAccount extends Account {
    private static final long serialVersionUID = 1L;
    private static final BigDecimal MAINTENANCE_FEE = new BigDecimal("50.00");

    protected CurrentAccount() {
        super();
    }

    public CurrentAccount(String accountNumber, User owner, BigDecimal openingBalance) {
        super(accountNumber, owner, openingBalance);
    }

    public CurrentAccount(int accountId, String accountNumber, User owner, BigDecimal balance, boolean active) {
        super(accountId, accountNumber, owner, balance, active);
    }

    @Override
    public AccountType getAccountType() {
        return AccountType.CURRENT;
    }

    @Override
    public BigDecimal calculateMaintenanceFee() {
        return MAINTENANCE_FEE;
    }
}
