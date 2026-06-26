package com.securebank.pro.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.securebank.pro.entity.Transaction;
import com.securebank.pro.enums.TransactionType;
import com.securebank.pro.repository.TransactionRepository;
import com.securebank.pro.service.TransactionService;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void addTransaction(Transaction transaction) {
        transactionRepository.save(transaction);
    }

    @Override
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    @Override
    public List<Transaction> sortTransactions(List<Transaction> transactions, Comparator<Transaction> comparator) {
        if (transactions == null) return Collections.emptyList();
        List<Transaction> sorted = new ArrayList<>(transactions);
        sorted.sort(comparator);
        return sorted;
    }

    @Override
    public List<Transaction> sortTransactionsNatural(List<Transaction> transactions) {
        if (transactions == null) return Collections.emptyList();
        List<Transaction> sorted = new ArrayList<>(transactions);
        Collections.sort(sorted); // Uses Comparable's compareTo
        return sorted;
    }

    @Override
    public List<Transaction> filterTransactions(List<Transaction> transactions, TransactionType type) {
        if (transactions == null) return Collections.emptyList();
        List<Transaction> filtered = new ArrayList<>();
        for (Transaction tx : transactions) {
            if (tx.getTransactionType() == type) {
                filtered.add(tx);
            }
        }
        return filtered;
    }
}
