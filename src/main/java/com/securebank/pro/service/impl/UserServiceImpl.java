package com.securebank.pro.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.securebank.pro.entity.User;
import com.securebank.pro.repository.UserRepository;
import com.securebank.pro.service.UserService;
import com.securebank.pro.util.BankLogger;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void addUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }
        if (!user.isRegistered()) {
            user.registerUser();
        }
        if (user.getPassword() != null && !isBCrypt(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        userRepository.save(user);
        BankLogger.info("User added successfully: '" + user.getFullName() + "' (" + user.getEmail() + ")");
    }

    @Override
    public String encryptPassword(String password) {
        if (password == null) {
            return null;
        }
        if (isBCrypt(password)) {
            return password;
        }
        return passwordEncoder.encode(password);
    }

    private boolean isBCrypt(String password) {
        return password != null && (password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$"));
    }

    @Override
    public void removeUser(int userId) {
        userRepository.deleteById(userId);
        BankLogger.info("User with ID '" + userId + "' was successfully removed.");
    }

    @Override
    public List<User> searchUser(String query) {
        return userRepository.search(query);
    }

    @Override
    public User getUserById(int userId) {
        return userRepository.findById(userId);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
