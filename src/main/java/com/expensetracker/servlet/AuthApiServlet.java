package com.expensetracker.servlet;

import com.expensetracker.dao.UserDao;
import com.expensetracker.model.User;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.util.ApiException;
import com.expensetracker.util.JsonUtil;
import com.expensetracker.util.PasswordUtil;
import com.expensetracker.util.StringUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet("/api/auth/*")
public class AuthApiServlet extends BaseApiServlet {

    private static final String DUMMY_HASH = PasswordUtil.hash("This is a fallbaclk not a password");
    private final UserRepository userDAO = new UserDao();

    static class Credentials {
        String name;
        String email;
        String password;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if("/register".equals(path)) {
            register(req, resp);
        } else if ("/login".equals(path)) {
            login(req,resp);
        } else {
            throw new ApiException(404, "Not found");
        }
    }

    private void register(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Credentials cred = JsonUtil.readBody(req, Credentials.class);
        String name = StringUtil.trim(cred.name);
        String email = cred.email == null ? null : cred.email.trim().toLowerCase();
        String password = cred.password;

        Map<String, String> errors = new LinkedHashMap<>();
        if(name == null || name.isEmpty())
            errors.put("name", "is required");
        else if (name.length() > 100)
            errors.put("name", "max 100 charactes only allowed");
        if (email == null || email.isEmpty())
            errors.put("email", "is required");
        else if (email.length() > 150 || !email.contains("@") || email.startsWith("@") || email.endsWith("@"))
            errors.put("email", "is not a valid email");

        if (password == null || password.length() < 8) {
            errors.put("password", "must be at least 8 characters");
        } else if (password.length() > 72) {
            errors.put("password", "must be at most 72 characters");
        }

        if (!errors.isEmpty()) {
            throw new ApiException(400, "Validation failed", errors);
        }

        if (userDAO.findByEmail(email) != null) {
            throw new ApiException(409, "User already registered with this email");
        }

        try {
            User user = userDAO.create(name, email, PasswordUtil.hash(password));
            JsonUtil.write(resp, 201, user);
        } catch (Exception e) {
            if (e.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new ApiException(409, "Email is already registered");
            }
            throw e;
        }

    }

    private void login(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Credentials cred = JsonUtil.readBody(req, Credentials.class);
        String email = cred.email == null ? "" : cred.email.trim().toLowerCase();
        String password = cred.password == null ? "" : cred.password;
        User user = userDAO.findByEmail(email);
        boolean passCheck = PasswordUtil.mathches(password, user == null ? DUMMY_HASH : user.getPasswordHash());
        if(user == null || !passCheck) {
            throw new ApiException(401, "Invalid email or password");
        }
        JsonUtil.write(resp,200, user);
    }

}
