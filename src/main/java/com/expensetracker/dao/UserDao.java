package com.expensetracker.dao;

import com.expensetracker.model.User;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.util.DBUtil;
import com.expensetracker.util.DataException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.zip.DataFormatException;

public class UserDao implements UserRepository {
    @Override
    public User findByEmail(String email) {
        String sql = "SELECT id, name,email, password_hash FROM users WHERE email = ?";
        try(Connection c = DBUtil.getConnection();
            PreparedStatement ps = c.prepareStatement(sql);
        ) {
            ps.setString(1, email);
            try(ResultSet rs = ps.executeQuery()) {
                if(rs.next()) {
                    User u = new User();
                    u.setId(rs.getLong("id"));
                    u.setName(rs.getString("name"));
                    u.setEmail(rs.getString("email"));
                    u.setPasswordHash(rs.getString("password_hash"));
                    return u;
                }
                return null;
            }
        }catch (Exception e) {
            throw new DataException("Failed to find user", e);
        }
    }

    @Override
    public User create(String name, String email, String passwordHash){
        String sql = "Insert INTO users(name, email, password_hash) VALUES(?,?,?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, passwordHash);
            ps.executeUpdate();
            try(ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return new User(keys.getLong(1), name, email);
            }
        } catch (Exception e) {
            throw new DataException("Failed to crate user", e);
        }
    }
}
