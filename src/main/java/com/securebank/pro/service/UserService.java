package com.securebank.pro.service;

import java.util.List;
import com.securebank.pro.entity.User;

public interface UserService {
    void addUser(User user);
    void removeUser(int userId);
    List<User> searchUser(String query);
    User getUserById(int userId);
    User getUserByEmail(String email);
    String encryptPassword(String password);
}
