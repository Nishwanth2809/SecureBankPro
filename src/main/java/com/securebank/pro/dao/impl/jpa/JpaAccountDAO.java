package com.securebank.pro.dao.impl.jpa;

import java.math.BigDecimal;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import com.securebank.pro.dao.AccountDAO;
import com.securebank.pro.entity.Account;
import com.securebank.pro.repository.jpa.AccountJpaRepository;

@Repository
@Primary
public class JpaAccountDAO implements AccountDAO {

    private final AccountJpaRepository accountJpaRepository;

    public JpaAccountDAO(AccountJpaRepository accountJpaRepository) {
        this.accountJpaRepository = accountJpaRepository;
    }

    @Override
    public void createAccount(Account account) {
        accountJpaRepository.save(account);
    }

    @Override
    public void updateBalance(String accountNumber, BigDecimal newBalance) {
        Account account = accountJpaRepository.findByAccountNumber(accountNumber);
        if (account != null) {
            BigDecimal currentBalance = account.getBalance();
            BigDecimal diff = newBalance.subtract(currentBalance);
            account.updateBalance(diff);
            accountJpaRepository.save(account);
        }
    }

    @Override
    public Account fetchAccount(String accountNumber) {
        return accountJpaRepository.findByAccountNumber(accountNumber);
    }
}
