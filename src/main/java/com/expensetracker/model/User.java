package com.expensetracker.model;

public class User {
    //Encapsulation - Restriction of external objects to access internal state
    // No un auth modification of data
    private long id;
    private String name;
    private String email;
    // exclude from serialization (ie) conversion of JSON
    private transient String passwordHash;

    public User() {
    }

    public User(long id, String name, String email) {
        // Checks vaildation along with setter reduces redundancy.
        setId(id);
        setName(name);
        setEmail(email);
    }

    public long getId() { return id; }

    public void setId(long id) {
        if (id < 0) {
            throw new IllegalArgumentException("id must not be negative");
        }
        this.id = id;
    }

    public String getName() { return name; }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        name = name.trim();
        if (name.length() > 100) {
            throw new IllegalArgumentException("name must be at most 100 characters");
        }
        this.name = name;
    }

    public String getEmail() { return email; }

    public void setEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email is required");
        }
        email = email.trim().toLowerCase();
        if (email.length() > 150 || !email.contains("@") || email.startsWith("@") || email.endsWith("@")) {
            throw new IllegalArgumentException("email is not valid");
        }
        this.email = email;
    }

    public String getPasswordHash() { return passwordHash; }

    public void setPasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("password hash is required");
        }
        this.passwordHash = passwordHash;
    }
}
