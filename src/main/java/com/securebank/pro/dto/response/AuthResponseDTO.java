package com.securebank.pro.dto.response;

public class AuthResponseDTO {
    private String email;
    private boolean authenticated;
    private String message;
    private String token;

    public AuthResponseDTO() {}

    public AuthResponseDTO(String email, boolean authenticated, String message) {
        this.email = email;
        this.authenticated = authenticated;
        this.message = message;
        this.token = null;
    }

    public AuthResponseDTO(String email, boolean authenticated, String message, String token) {
        this.email = email;
        this.authenticated = authenticated;
        this.message = message;
        this.token = token;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isAuthenticated() { return authenticated; }
    public void setAuthenticated(boolean authenticated) { this.authenticated = authenticated; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
