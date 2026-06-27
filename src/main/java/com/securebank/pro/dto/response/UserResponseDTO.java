package com.securebank.pro.dto.response;

import java.util.List;

public class UserResponseDTO {
    private int userId;
    private String fullName;
    private String email;
    private String role;
    private List<String> accountNumbers;

    public UserResponseDTO() {}

    public UserResponseDTO(int userId, String fullName, String email, String role, List<String> accountNumbers) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.accountNumbers = accountNumbers;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public List<String> getAccountNumbers() { return accountNumbers; }
    public void setAccountNumbers(List<String> accountNumbers) { this.accountNumbers = accountNumbers; }
}
