package com.securebank.pro.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.securebank.pro.entity.Account;
import java.util.List;

public interface AccountJpaRepository extends JpaRepository<Account, Integer> {
    Account findByAccountNumber(String accountNumber);

    @Query("SELECT a FROM Account a WHERE a.owner.userId = :ownerId")
    List<Account> findByOwnerUserId(@Param("ownerId") int ownerId);
}
