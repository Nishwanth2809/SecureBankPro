package com.securebank.pro.repository.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.securebank.pro.entity.Admin;
import com.securebank.pro.repository.AdminRepository;

public class InMemoryAdminRepository implements AdminRepository {

    private static final InMemoryAdminRepository INSTANCE = new InMemoryAdminRepository();

    private final Map<String, Admin> adminsByEmail = new HashMap<>();

    private InMemoryAdminRepository() {}

    public static InMemoryAdminRepository getInstance() {
        return INSTANCE;
    }

    @Override
    public synchronized void save(Admin admin) {
        if (admin == null) {
            throw new IllegalArgumentException("Admin cannot be null.");
        }
        adminsByEmail.put(admin.getEmail().toLowerCase().trim(), admin);
    }

    @Override
    public synchronized Admin findByEmail(String email) {
        if (email == null) return null;
        return adminsByEmail.get(email.toLowerCase().trim());
    }

    @Override
    public synchronized List<Admin> findAll() {
        return new ArrayList<>(adminsByEmail.values());
    }

    @Override
    public synchronized void clear() {
        adminsByEmail.clear();
    }
}
