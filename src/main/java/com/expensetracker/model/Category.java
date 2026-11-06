package com.expensetracker.model;

public class Category {
    private long id;
    private String name;

    public Category() {
    }

    public Category(long id, String name) {
        setId(id);
        setName(name);
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
        if (name.length() > 50) {
            throw new IllegalArgumentException("name must be at most 50 characters");
        }
        this.name = name;
    }
}
