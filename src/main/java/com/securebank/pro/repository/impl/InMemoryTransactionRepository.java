package com.securebank.pro.repository.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.securebank.pro.entity.Account;
import com.securebank.pro.entity.Transaction;
import com.securebank.pro.repository.TransactionRepository;

public class InMemoryTransactionRepository implements TransactionRepository {

    private static final InMemoryTransactionRepository INSTANCE = new InMemoryTransactionRepository();

    private final List<Transaction> transactions = new ArrayList<>();
    private final Queue<Transaction> pendingQueue = new LinkedList<>();

    private InMemoryTransactionRepository() {}

    public static InMemoryTransactionRepository getInstance() {
        return INSTANCE;
    }

    @Override
    public synchronized void save(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction cannot be null.");
        }
        transactions.add(transaction);
    }

    @Override
    public synchronized List<Transaction> findAll() {
        return new ArrayList<>(transactions);
    }

    @Override
    public synchronized List<Transaction> findByAccount(Account account) {
        if (account == null) {
            return Collections.emptyList();
        }
        List<Transaction> results = new ArrayList<>();
        for (Transaction tx : transactions) {
            if (tx.belongsTo(account)) {
                results.add(tx);
            }
        }
        return results;
    }

    @Override
    public synchronized void queuePending(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Cannot queue a null transaction.");
        }
        pendingQueue.offer(transaction);
    }

    @Override
    public synchronized Transaction pollPending() {
        return pendingQueue.poll();
    }

    @Override
    public synchronized Transaction peekPending() {
        return pendingQueue.peek();
    }

    @Override
    public synchronized Queue<Transaction> getPendingQueue() {
        return pendingQueue; // Return queue for demo/processing purposes
    }

    @Override
    public synchronized void clear() {
        transactions.clear();
        pendingQueue.clear();
    }
}
