package com.securebank.pro.service;

import java.math.BigDecimal;
import java.util.List;
import com.securebank.pro.entity.Account;

public interface AccountService {
    void createAccount(Account account);
    Account fetchAccount(String accountNumber);
    void updateBalance(String accountNumber, BigDecimal newBalance);
    List<Account> getAllAccounts();
    void freezeAccount(String accountNumber);
    void unblockAccount(String accountNumber);
    void deleteAccount(String accountNumber);
}
