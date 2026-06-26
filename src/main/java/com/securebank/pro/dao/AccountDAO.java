package com.securebank.pro.dao;

import java.math.BigDecimal;
import com.securebank.pro.entity.Account;

public interface AccountDAO {
    void createAccount(Account account);
    void updateBalance(String accountNumber, BigDecimal newBalance);
    Account fetchAccount(String accountNumber);
}
