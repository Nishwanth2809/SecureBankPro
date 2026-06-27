package com.securebank.pro.service.impl;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.securebank.pro.dao.UserDAO;
import com.securebank.pro.entity.User;
import com.securebank.pro.service.AuthService;
import com.securebank.pro.util.JwtTokenUtil;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class AuthServiceImpl implements AuthService {

    private final Set<String> activeSessions = new HashSet<>();
    private final Queue<String> sessionActivityLog = new LinkedList<>();
    private final UserDAO userDAO;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;

    public AuthServiceImpl(UserDAO userDAO, PasswordEncoder passwordEncoder, JwtTokenUtil jwtTokenUtil) {
        this.userDAO = userDAO;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    public synchronized boolean login(String email, String password) {
        if (email == null) {
            logActivity("FAILED LOGIN: Email was null.");
            return false;
        }

        String formattedEmail = email.toLowerCase().trim();
        User user = userDAO.findUserByEmail(formattedEmail);
        if (user == null) {
            logActivity("FAILED LOGIN: Email '" + email + "' not registered.");
            return false;
        }

        boolean authenticated = user.loginUser(formattedEmail, password, passwordEncoder);
        if (authenticated) {
            activeSessions.add(formattedEmail);
            logActivity("LOGIN SUCCESS: " + formattedEmail);
        } else {
            logActivity("FAILED LOGIN: Invalid credentials for '" + formattedEmail + "'");
        }
        return authenticated;
    }

    @Override
    public synchronized String authenticateUser(String email, String password) {
        boolean authenticated = login(email, password);
        if (authenticated) {
            User user = userDAO.findUserByEmail(email.toLowerCase().trim());
            String role = (user != null && user.getRole() != null) ? user.getRole().name() : "CUSTOMER";
            return jwtTokenUtil.generateToken(email.toLowerCase().trim(), role);
        }
        return null;
    }

    @Override
    public synchronized void logout(String email) {
        if (email == null) return;
        String formattedEmail = email.toLowerCase().trim();
        if (activeSessions.remove(formattedEmail)) {
            User user = userDAO.findUserByEmail(formattedEmail);
            if (user != null) {
                user.logoutUser();
            }
            logActivity("LOGOUT: " + formattedEmail);
        }
    }

    @Override
    public synchronized boolean isSessionActive(String email) {
        if (email == null) return false;
        return activeSessions.contains(email.toLowerCase().trim());
    }

    @Override
    public synchronized Queue<String> getSessionActivityLog() {
        return new LinkedList<>(sessionActivityLog); // Return copy for safety
    }

    private void logActivity(String event) {
        sessionActivityLog.offer(event);
        if (sessionActivityLog.size() > 10) {
            sessionActivityLog.poll(); // Keep only the last 10 activities (FIFO)
        }
        if (event.contains("FAILED")) {
            com.securebank.pro.util.BankLogger.warn("[Auth] " + event);
        } else {
            com.securebank.pro.util.BankLogger.info("[Auth] " + event);
        }
    }
}
