package com.securebank.pro.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import com.securebank.pro.entity.Admin;
import java.util.Optional;

public interface AdminJpaRepository extends JpaRepository<Admin, Integer> {
    Optional<Admin> findByEmail(String email);
}
