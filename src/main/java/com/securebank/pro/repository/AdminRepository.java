package com.securebank.pro.repository;

import java.util.List;
import com.securebank.pro.entity.Admin;

public interface AdminRepository {
    void save(Admin admin);
    Admin findByEmail(String email);
    List<Admin> findAll();
    void clear();
}
