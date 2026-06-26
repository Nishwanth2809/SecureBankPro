package com.securebank.pro.repository;

import java.util.List;
import com.securebank.pro.entity.User;

public interface UserRepository {
    void save(User user);
    void deleteById(int userId);
    User findById(int userId);
    User findByEmail(String email);
    List<User> search(String query);
    List<User> findAll();
    void clear();
}
