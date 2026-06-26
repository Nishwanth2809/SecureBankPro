package com.securebank.pro.repository;

import java.util.List;
import java.util.Queue;
import com.securebank.pro.entity.Account;
import com.securebank.pro.entity.Transaction;

public interface TransactionRepository {
    void save(Transaction transaction);
    List<Transaction> findAll();
    List<Transaction> findByAccount(Account account);
    
    void queuePending(Transaction transaction);
    Transaction pollPending();
    Transaction peekPending();
    Queue<Transaction> getPendingQueue();
    void clear();
}
