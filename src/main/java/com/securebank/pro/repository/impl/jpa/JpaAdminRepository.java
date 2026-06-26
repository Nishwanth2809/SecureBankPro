package com.securebank.pro.repository.impl.jpa;

import java.util.List;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import com.securebank.pro.entity.Admin;
import com.securebank.pro.repository.AdminRepository;
import com.securebank.pro.repository.jpa.AdminJpaRepository;

@Repository
@Primary
public class JpaAdminRepository implements AdminRepository {

    private final AdminJpaRepository adminJpaRepository;

    public JpaAdminRepository(AdminJpaRepository adminJpaRepository) {
        this.adminJpaRepository = adminJpaRepository;
    }

    @Override
    public void save(Admin admin) {
        adminJpaRepository.save(admin);
    }

    @Override
    public Admin findByEmail(String email) {
        return adminJpaRepository.findByEmail(email).orElse(null);
    }

    @Override
    public List<Admin> findAll() {
        return adminJpaRepository.findAll();
    }

    @Override
    public void clear() {
        adminJpaRepository.deleteAllInBatch();
    }
}
