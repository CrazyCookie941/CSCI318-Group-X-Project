package com.csci318.groupx.registrationservice;

public class Account {

    // Changed to protected so subclasses can inherit and access them directly
    protected String email;
    protected String username;
    protected String location;
    protected String password;

    // Constructor
    public Account(String email, String username, String location, String password) {
        this.email = email;
        this.username = username;
        this.location = location;
        this.password = password;
    }

    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}