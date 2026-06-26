package com.securebank.pro.dao.impl.jpa;

import java.util.List;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import com.securebank.pro.dao.TransactionDAO;
import com.securebank.pro.entity.Transaction;
import com.securebank.pro.repository.jpa.TransactionJpaRepository;

@Repository
@Primary
public class JpaTransactionDAO implements TransactionDAO {

    private final TransactionJpaRepository transactionJpaRepository;

    public JpaTransactionDAO(TransactionJpaRepository transactionJpaRepository) {
        this.transactionJpaRepository = transactionJpaRepository;
    }

    @Override
    public void saveTransaction(Transaction transaction) {
        transactionJpaRepository.save(transaction);
    }

    @Override
    public List<Transaction> getTransactionsByUser(int userId) {
        return transactionJpaRepository.findTransactionsByUser(userId);
    }
}
