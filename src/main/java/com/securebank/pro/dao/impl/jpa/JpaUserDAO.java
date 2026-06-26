package com.securebank.pro.dao.impl.jpa;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import com.securebank.pro.dao.UserDAO;
import com.securebank.pro.entity.User;
import com.securebank.pro.repository.jpa.UserJpaRepository;

@Component
@Primary
public class JpaUserDAO implements UserDAO {

    private final UserJpaRepository userJpaRepository;

    public JpaUserDAO(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public void saveUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }
        if (user.getEmail() != null) {
            userJpaRepository.findByEmail(user.getEmail()).ifPresent(existing -> {
                if (existing.getUserId() != user.getUserId()) {
                    throw new IllegalArgumentException("Email '" + user.getEmail() + "' is already registered.");
                }
            });
        }
        try {
            userJpaRepository.saveAndFlush(user);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Email '" + user.getEmail() + "' is already registered.", e);
        }
    }

    @Override
    public User findUserByEmail(String email) {
        return userJpaRepository.findByEmail(email).orElse(null);
    }

    @Override
    public void updateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }
        if (user.getEmail() != null) {
            userJpaRepository.findByEmail(user.getEmail()).ifPresent(existing -> {
                if (existing.getUserId() != user.getUserId()) {
                    throw new IllegalArgumentException("Email '" + user.getEmail() + "' is already registered.");
                }
            });
        }
        try {
            userJpaRepository.saveAndFlush(user);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Email '" + user.getEmail() + "' is already registered.", e);
        }
    }
}
