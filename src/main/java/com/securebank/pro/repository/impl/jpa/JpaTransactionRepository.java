package com.securebank.pro.repository.impl.jpa;

import java.util.List;
import java.util.Queue;
import java.util.LinkedList;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import com.securebank.pro.entity.Account;
import com.securebank.pro.entity.Transaction;
import com.securebank.pro.repository.TransactionRepository;
import com.securebank.pro.repository.jpa.TransactionJpaRepository;

@Repository
@Primary
public class JpaTransactionRepository implements TransactionRepository {

    private final TransactionJpaRepository transactionJpaRepository;
    private final Queue<Transaction> pendingQueue = new LinkedList<>();

    public JpaTransactionRepository(TransactionJpaRepository transactionJpaRepository) {
        this.transactionJpaRepository = transactionJpaRepository;
    }

    @Override
    public void save(Transaction transaction) {
        transactionJpaRepository.save(transaction);
    }

    @Override
    public List<Transaction> findAll() {
        return transactionJpaRepository.findAll();
    }

    @Override
    public List<Transaction> findByAccount(Account account) {
        if (account == null) return List.of();
        return transactionJpaRepository.findByAccount(account.getAccountId());
    }

    @Override
    public void queuePending(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Cannot queue a null transaction.");
        }
        pendingQueue.offer(transaction);
    }

    @Override
    public Transaction pollPending() {
        return pendingQueue.poll();
    }

    @Override
    public Transaction peekPending() {
        return pendingQueue.peek();
    }

    @Override
    public Queue<Transaction> getPendingQueue() {
        return pendingQueue;
    }

    @Override
    public void clear() {
        transactionJpaRepository.deleteAllInBatch();
        pendingQueue.clear();
    }
}
