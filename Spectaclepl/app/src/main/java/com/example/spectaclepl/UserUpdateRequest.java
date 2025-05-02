package com.example.spectaclepl;

public class UserUpdateRequest {
    private String username;
    private String email;
    private String phone;
    private String currentPassword;
    private String newPassword;

    public UserUpdateRequest(String username, String email, String phone, String currentPassword, String newPassword) {
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }

    // Getters et setters
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getCurrentPassword() { return currentPassword; }
    public String getNewPassword() { return newPassword; }
}