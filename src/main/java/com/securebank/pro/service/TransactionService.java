package com.securebank.pro.service;

import java.util.Comparator;
import java.util.List;
import com.securebank.pro.entity.Transaction;
import com.securebank.pro.enums.TransactionType;

public interface TransactionService {
    void addTransaction(Transaction transaction);
    List<Transaction> getAllTransactions();
    List<Transaction> sortTransactions(List<Transaction> transactions, Comparator<Transaction> comparator);
    List<Transaction> sortTransactionsNatural(List<Transaction> transactions);
    List<Transaction> filterTransactions(List<Transaction> transactions, TransactionType type);
}
