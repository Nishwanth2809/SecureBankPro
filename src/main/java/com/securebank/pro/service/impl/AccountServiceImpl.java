package com.securebank.pro.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.securebank.pro.entity.Account;
import com.securebank.pro.repository.AccountRepository;
import com.securebank.pro.service.AccountService;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public void createAccount(Account account) {
        accountRepository.save(account);
    }

    @Override
    public Account fetchAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }

    @Override
    public void updateBalance(String accountNumber, BigDecimal newBalance) {
        Account account = accountRepository.findByAccountNumber(accountNumber);
        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountNumber);
        }
        BigDecimal diff = newBalance.subtract(account.getBalance());
        account.updateBalance(diff);
        accountRepository.save(account);
    }

    @Override
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Override
    public void freezeAccount(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber);
        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountNumber);
        }
        account.freeze();
        accountRepository.save(account);
    }

    @Override
    public void unblockAccount(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber);
        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountNumber);
        }
        account.unblock();
        accountRepository.save(account);
    }

    @Override
    public void deleteAccount(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber);
        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountNumber);
        }
        accountRepository.deleteByAccountNumber(accountNumber);
    }
}
