package com.expensetracker.repository;

import com.expensetracker.model.User;

public interface UserRepository {
    User findByEmail(String email);

    User create(String name, String email, String passwordHash);
}
