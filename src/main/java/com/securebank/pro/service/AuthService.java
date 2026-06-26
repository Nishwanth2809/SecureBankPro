package com.securebank.pro.service;

import java.util.Queue;

public interface AuthService {
    boolean login(String email, String password);
    void logout(String email);
    boolean isSessionActive(String email);
    Queue<String> getSessionActivityLog();
    String authenticateUser(String email, String password);
}
