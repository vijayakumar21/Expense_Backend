package com.expensetracker.servlet;

import com.expensetracker.util.DBUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

// check the connection
@WebServlet("/api/ping")
public class PingServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        try (Connection c = DBUtil.getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM categories")) {
            rs.next();
            res.getWriter().write("{\"ok\":true,\"db\":\"connected\",\"categories\":" + rs.getInt(1) + "}");
        } catch (Exception e) {
            log("Ping failed", e);
            res.setStatus(500);
            res.getWriter().write("{\"ok\":false,\"error\":\"database unavailable\"}");
        }
    }
}
