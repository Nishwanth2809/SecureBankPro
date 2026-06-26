package com.securebank.pro.repository.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.securebank.pro.entity.Account;
import com.securebank.pro.exception.InvalidAccountException;
import com.securebank.pro.repository.AccountRepository;

public class InMemoryAccountRepository implements AccountRepository {

    private static final InMemoryAccountRepository INSTANCE = new InMemoryAccountRepository();

    private final Map<String, Account> accountsByNumber = new HashMap<>();

    private InMemoryAccountRepository() {}

    public static InMemoryAccountRepository getInstance() {
        return INSTANCE;
    }

    @Override
    public synchronized void save(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null.");
        }
        accountsByNumber.put(account.getAccountNumber().toUpperCase().trim(), account);
    }

    @Override
    public synchronized Account findByAccountNumber(String accountNumber) {
        if (accountNumber == null) return null;
        return accountsByNumber.get(accountNumber.toUpperCase().trim());
    }

    @Override
    public synchronized List<Account> findByOwnerId(int ownerId) {
        List<Account> results = new ArrayList<>();
        for (Account account : accountsByNumber.values()) {
            if (account.getOwner() != null && account.getOwner().getUserId() == ownerId) {
                results.add(account);
            }
        }
        return results;
    }

    @Override
    public synchronized List<Account> findAll() {
        return new ArrayList<>(accountsByNumber.values());
    }

    @Override
    public synchronized void deleteByAccountNumber(String accountNumber) {
        if (accountNumber == null) {
            throw new IllegalArgumentException("Account number cannot be null.");
        }
        String key = accountNumber.toUpperCase().trim();
        if (!accountsByNumber.containsKey(key)) {
            throw new InvalidAccountException(accountNumber, "Account not found.");
        }
        accountsByNumber.remove(key);
    }

    @Override
    public synchronized void clear() {
        accountsByNumber.clear();
    }
}
