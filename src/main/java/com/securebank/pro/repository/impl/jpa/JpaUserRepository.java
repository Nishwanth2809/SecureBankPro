package com.securebank.pro.repository.impl.jpa;

import java.util.List;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import com.securebank.pro.entity.User;
import com.securebank.pro.exception.UserNotFoundException;
import com.securebank.pro.repository.UserRepository;
import com.securebank.pro.repository.jpa.UserJpaRepository;

@Component
@Primary
public class JpaUserRepository implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    public JpaUserRepository(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public void save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }
        if (user.getEmail() != null) {
            User existing = findByEmail(user.getEmail());
            if (existing != null && existing.getUserId() != user.getUserId()) {
                throw new IllegalArgumentException("Email '" + user.getEmail() + "' is already registered.");
            }
        }
        try {
            userJpaRepository.saveAndFlush(user);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Email '" + user.getEmail() + "' is already registered.", e);
        }
    }

    @Override
    public void deleteById(int userId) {
        if (!userJpaRepository.existsById(userId)) {
            throw new UserNotFoundException("userId", String.valueOf(userId));
        }
        userJpaRepository.deleteById(userId);
    }

    @Override
    public User findById(int userId) {
        return userJpaRepository.findById(userId).orElse(null);
    }

    @Override
    public User findByEmail(String email) {
        return userJpaRepository.findByEmail(email).orElse(null);
    }

    @Override
    public List<User> search(String query) {
        return userJpaRepository.search(query);
    }

    @Override
    public List<User> findAll() {
        return userJpaRepository.findAll();
    }

    @Override
    public void clear() {
        userJpaRepository.deleteAllInBatch();
    }
}
