package com.securebank.pro.repository;

import java.util.List;
import com.securebank.pro.entity.Account;

public interface AccountRepository {
    void save(Account account);
    Account findByAccountNumber(String accountNumber);
    List<Account> findByOwnerId(int ownerId);
    List<Account> findAll();
    void deleteByAccountNumber(String accountNumber);
    void clear();
}
