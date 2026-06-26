package com.securebank.pro.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.securebank.pro.entity.Transaction;
import java.util.List;

public interface TransactionJpaRepository extends JpaRepository<Transaction, Integer> {
    @Query("SELECT t FROM Transaction t " +
           "LEFT JOIN t.sourceAccount sa " +
           "LEFT JOIN sa.owner sao " +
           "LEFT JOIN t.destinationAccount da " +
           "LEFT JOIN da.owner dao " +
           "WHERE sao.userId = :userId OR dao.userId = :userId")
    List<Transaction> findTransactionsByUser(@Param("userId") int userId);

    @Query("SELECT t FROM Transaction t " +
           "LEFT JOIN t.sourceAccount sa " +
           "LEFT JOIN t.destinationAccount da " +
           "WHERE sa.accountId = :accountId OR da.accountId = :accountId")
    List<Transaction> findByAccount(@Param("accountId") int accountId);
}
