package com.securebank.pro.dto;

import java.io.Serializable;
import java.util.List;

import com.securebank.pro.entity.Account;
import com.securebank.pro.entity.Transaction;
import com.securebank.pro.entity.User;

public class DatabaseBackup implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<User> users;
    private final List<Account> accounts;
    private final List<Transaction> transactions;

    public DatabaseBackup(List<User> users, List<Account> accounts, List<Transaction> transactions) {
        this.users = users;
        this.accounts = accounts;
        this.transactions = transactions;
    }

    public List<User> getUsers() {
        return users;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }
}
