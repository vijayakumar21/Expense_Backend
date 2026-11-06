package com.expensetracker.dao;

import com.expensetracker.model.Report;
import com.expensetracker.util.DBUtil;
import com.expensetracker.util.DataException;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportsDao {
    private static final String SELECT_WITH_TOTALS = "SELECT r.id, r.user_id, r.name, r.description, r.start_date,r.end_Date,"
            +"r.budget, COALESCE(SUM(e.amount), 0) AS total_spent, COUNT(e.id) AS expense_count " +
            "FROM reports r LEFT JOIN expenses e ON e.report_id = r.id ";
    private static final String GROUP_BY =
            "GROUP BY r.id, r.user_id, r.name, r.description, r.start_date, r.end_date, r.budget ";


    private Report map(ResultSet rs) throws SQLException {
        Report r = new Report();
        r.setId(rs.getLong("id"));
        r.setUserId(rs.getLong("user_id"));
        r.setName(rs.getString("name"));
        r.setDescription(rs.getString("description"));
        Date start = rs.getDate("start_date");
        r.setStartDate(start == null ? null : start.toLocalDate());
        Date end = rs.getDate("end_date");
        r.setEndDate(end == null ? null : end.toLocalDate());
        r.setBudget(rs.getBigDecimal("budget"));
        r.setTotalSpent(rs.getBigDecimal("total_spent"));
        r.setExpenseCount(rs.getInt("expense_count"));
        return r;
    }

    public List<Report> findAll(long userId) {
        String query = SELECT_WITH_TOTALS + "WHERE r.user_id = ? " + GROUP_BY + "ORDER BY r.start_date DESC, r.id DESC";
        List<Report> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(query)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DataException("Failed to fetch Reports List", e);
        }
    }

    public Report findBy(long id, long userId) {
        String query = SELECT_WITH_TOTALS + "WHERE r.id = ? AND r.user_id = ? " + GROUP_BY;
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(query)) {
            ps.setLong(1, id);
            ps.setLong(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DataException("Failed to find report", e);
        }
    }

    public long create(Report r) {
        String query = "INSERT INTO reports(name, description, start_date, end_date, budget, user_id) VALUES (?,?,?,?,?,?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            Date startDate = r.getStartDate() == null ? null : Date.valueOf(r.getStartDate());
            Date endDate = r.getEndDate() == null ? null : Date.valueOf(r.getEndDate());
            ps.setString(1, r.getName());
            ps.setString(2, r.getDescription());
            ps.setDate(3, startDate);
            ps.setDate(4, endDate);
            ps.setBigDecimal(5, r.getBudget());
            ps.setLong(6, r.getUserId());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                // returns the Report ID
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new DataException("Failed to create report", e);
        }
    }

    public boolean update(Report r){
        String query = "UPDATE reports SET name = ?, description = ?, start_date = ?, end_date = ?, budget = ? WHERE id = ? AND user_id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(query)) {
            Date startDate = r.getStartDate() == null ? null : Date.valueOf(r.getStartDate());
            Date endDate = r.getEndDate() == null ? null : Date.valueOf(r.getEndDate());
            ps.setString(1, r.getName());
            ps.setString(2, r.getDescription());
            ps.setDate(3, startDate);
            ps.setDate(4, endDate);
            ps.setBigDecimal(5, r.getBudget());
            ps.setLong(6, r.getId());
            ps.setLong(7, r.getUserId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataException("Failed to update report", e);
        }
    }

    public boolean delete(long id, long userId, boolean deleteExpenses) {
        try (Connection c = DBUtil.getConnection()) {
            c.setAutoCommit(false);
            try {
                if (deleteExpenses) {
                    try (PreparedStatement ps = c.prepareStatement("DELETE FROM expenses WHERE report_id = ? AND user_id = ?")) {
                        ps.setLong(1, id);
                        ps.setLong(2, userId);
                        ps.executeUpdate();
                    }
                }
                int noRows;
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM reports WHERE id = ? AND user_id = ?")) {
                    ps.setLong(1, id);
                    ps.setLong(2, userId);
                    noRows = ps.executeUpdate();
                }
                c.commit();
                return noRows > 0;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataException("Failed to delete report", e);
        }
    }
}
