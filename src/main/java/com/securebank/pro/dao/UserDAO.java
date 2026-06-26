package com.securebank.pro.dao;

import com.securebank.pro.entity.User;

public interface UserDAO {
    void saveUser(User user);
    User findUserByEmail(String email);
    void updateUser(User user);
}
