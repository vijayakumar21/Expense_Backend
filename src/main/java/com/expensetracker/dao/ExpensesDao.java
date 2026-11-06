package com.expensetracker.dao;

import com.expensetracker.model.Expense;
import com.expensetracker.util.DBUtil;
import com.expensetracker.util.DataException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExpensesDao {
    private static final String SELECT_WITH_NAMES = "SELECT e.id, e.user_id, e.report_id, e.category_id, e.title, e.amount, " +
            "e.expense_date, e.notes, r.name AS report_name, c.name AS category_name " +
            "FROM expenses e LEFT JOIN reports r ON r.id = e.report_id " +
            "LEFT JOIN categories c ON c.id = e.category_id ";

    // to give and rise what is missing in the message. Basically checks based on what foreign key the error has occured.
    private String referenceError(SQLIntegrityConstraintViolationException ex) {
        String message = ex.getMessage() == null ? "" : ex.getMessage();
        if (message.contains("category_id")) {
            return "categoryId does not exist";
        }
        if (message.contains("report_id")) {
            return "reportId does not exist";
        }
        return "reportId or categoryId does not exist";
    }

    private Expense map(ResultSet rs) throws SQLException {
        Expense e = new Expense();
        e.setId(rs.getLong("id"));
        e.setUserId(rs.getLong("user_id"));
        long reportId = rs.getLong("report_id");
        e.setReportId(rs.wasNull() ? null : reportId);
        long categoryId = rs.getLong("category_id");
        e.setCategoryId(rs.wasNull() ? null : categoryId);
        e.setTitle(rs.getString("title"));
        e.setAmount(rs.getBigDecimal("amount"));
        e.setExpenseDate(rs.getDate("expense_date").toLocalDate());
        e.setNotes(rs.getString("notes"));
        e.setReportName(rs.getString("report_name"));
        e.setCategoryName(rs.getString("category_name"));
        return e;
    }

    public List<Expense> findAll(long userId, Long reportId, Long categoryId) {
        StringBuilder query = new StringBuilder(SELECT_WITH_NAMES + "WHERE e.user_id = ? ");
        if (reportId != null) {
            query.append("AND e.report_id = ? ");
        }
        if (categoryId != null) {
            query.append("AND e.category_id = ? ");
        }
        query.append("ORDER BY e.expense_date DESC, e.id DESC");
        List<Expense> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(query.toString())) {
            int index = 1;
            ps.setLong(index++, userId);
            if (reportId != null) {
                ps.setLong(index++, reportId);
            }
            if (categoryId != null) {
                ps.setLong(index, categoryId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DataException("Failed to fetch Expenses List", e);
        }
    }

    public Expense findBy(long id, long userId) {
        String query = SELECT_WITH_NAMES + "WHERE e.id = ? AND e.user_id = ?";
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
            throw new DataException("Failed to find expense", e);
        }
    }

    public long create(Expense e) {
        String query = "INSERT INTO expenses(report_id, category_id, title, amount, expense_date, notes, user_id) VALUES (?,?,?,?,?,?,?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, e);
            ps.setLong(7, e.getUserId());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                // returns the Expense ID
                return rs.getLong(1);
            }
        } catch (SQLIntegrityConstraintViolationException ex) {
            // if there is no math=ching entry for categoryId or reportID (Foreign key constraint check)
            throw new IllegalArgumentException(referenceError(ex));
        } catch (SQLException ex) {
            throw new DataException("Failed to create expense", ex);
        }
    }

    public boolean update(Expense e) {
        String query = "UPDATE expenses SET report_id = ?, category_id = ?, title = ?, amount = ?, expense_date = ?, notes = ? WHERE id = ? AND user_id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(query)) {
            bind(ps, e);
            ps.setLong(7, e.getId());
            ps.setLong(8, e.getUserId());
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException ex) {
            throw new IllegalArgumentException(referenceError(ex));
        } catch (SQLException ex) {
            throw new DataException("Failed to update expense", ex);
        }
    }

    public boolean delete(long id, long userId) {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM expenses WHERE id = ? AND user_id = ?")) {
            ps.setLong(1, id);
            ps.setLong(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataException("Failed to delete expense", e);
        }
    }

    private void bind(PreparedStatement ps, Expense e) throws SQLException {
        if (e.getReportId() == null) {
            ps.setNull(1, Types.BIGINT);
        } else {
            ps.setLong(1, e.getReportId());
        }
        if (e.getCategoryId() == null) {
            ps.setNull(2, Types.BIGINT);
        } else {
            ps.setLong(2, e.getCategoryId());
        }
        ps.setString(3, e.getTitle());
        ps.setBigDecimal(4, e.getAmount());
        ps.setDate(5, Date.valueOf(e.getExpenseDate()));
        ps.setString(6, e.getNotes());
    }
}
