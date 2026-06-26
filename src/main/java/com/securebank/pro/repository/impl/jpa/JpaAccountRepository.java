package com.securebank.pro.repository.impl.jpa;

import java.util.List;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import com.securebank.pro.entity.Account;
import com.securebank.pro.repository.AccountRepository;
import com.securebank.pro.repository.jpa.AccountJpaRepository;

@Repository
@Primary
public class JpaAccountRepository implements AccountRepository {

    private final AccountJpaRepository accountJpaRepository;

    public JpaAccountRepository(AccountJpaRepository accountJpaRepository) {
        this.accountJpaRepository = accountJpaRepository;
    }

    @Override
    public void save(Account account) {
        accountJpaRepository.save(account);
    }

    @Override
    public Account findByAccountNumber(String accountNumber) {
        return accountJpaRepository.findByAccountNumber(accountNumber);
    }

    @Override
    public List<Account> findByOwnerId(int ownerId) {
        return accountJpaRepository.findByOwnerUserId(ownerId);
    }

    @Override
    public List<Account> findAll() {
        return accountJpaRepository.findAll();
    }

    @Override
    public void deleteByAccountNumber(String accountNumber) {
        Account account = findByAccountNumber(accountNumber);
        if (account != null) {
            accountJpaRepository.delete(account);
        }
    }

    @Override
    public void clear() {
        accountJpaRepository.deleteAllInBatch();
    }
}
