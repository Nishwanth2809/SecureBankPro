package com.securebank.pro.dao;

import java.util.List;
import com.securebank.pro.entity.Transaction;

public interface TransactionDAO {
    void saveTransaction(Transaction transaction);
    List<Transaction> getTransactionsByUser(int userId);
}
