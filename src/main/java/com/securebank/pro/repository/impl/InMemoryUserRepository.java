package com.securebank.pro.repository.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.securebank.pro.entity.User;
import com.securebank.pro.exception.UserNotFoundException;
import com.securebank.pro.repository.UserRepository;

public class InMemoryUserRepository implements UserRepository {

    private static final InMemoryUserRepository INSTANCE = new InMemoryUserRepository();

    private final Map<Integer, User> usersById = new HashMap<>();
    private final Map<String, User> usersByEmail = new HashMap<>();

    private InMemoryUserRepository() {}

    public static InMemoryUserRepository getInstance() {
        return INSTANCE;
    }

    @Override
    public synchronized void save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }
        
        // Email uniqueness validation
        User existing = usersByEmail.get(user.getEmail());
        if (existing != null && existing.getUserId() != user.getUserId()) {
            throw new IllegalArgumentException("Email '" + user.getEmail() + "' is already registered.");
        }

        // Remove old mapping from email map if user already exists with a different email
        User oldUser = usersById.get(user.getUserId());
        if (oldUser != null) {
            usersByEmail.remove(oldUser.getEmail());
        }

        usersById.put(user.getUserId(), user);
        usersByEmail.put(user.getEmail(), user);
    }

    @Override
    public synchronized void deleteById(int userId) {
        User user = usersById.get(userId);
        if (user == null) {
            throw new UserNotFoundException("userId", String.valueOf(userId));
        }
        usersById.remove(userId);
        usersByEmail.remove(user.getEmail());
    }

    @Override
    public synchronized User findById(int userId) {
        return usersById.get(userId);
    }

    @Override
    public synchronized User findByEmail(String email) {
        if (email == null) return null;
        return usersByEmail.get(email.toLowerCase().trim());
    }

    @Override
    public synchronized List<User> search(String query) {
        List<User> results = new ArrayList<>();
        if (query == null || query.isBlank()) {
            return results;
        }

        String lowerQuery = query.toLowerCase().trim();
        
        // Demonstrating manual Iterator usage
        Iterator<User> iterator = usersById.values().iterator();
        while (iterator.hasNext()) {
            User user = iterator.next();
            if ((user.getFullName() != null && user.getFullName().toLowerCase().contains(lowerQuery)) ||
                (user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerQuery))) {
                results.add(user);
            }
        }
        return results;
    }

    @Override
    public synchronized List<User> findAll() {
        return new ArrayList<>(usersById.values());
    }

    @Override
    public synchronized void clear() {
        usersById.clear();
        usersByEmail.clear();
    }
}
