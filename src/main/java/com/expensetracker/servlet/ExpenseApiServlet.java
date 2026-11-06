package com.expensetracker.servlet;

import com.expensetracker.dao.ExpensesDao;
import com.expensetracker.dao.ReportsDao;
import com.expensetracker.model.Expense;
import com.expensetracker.util.ApiException;
import com.expensetracker.util.JsonUtil;
import com.expensetracker.util.StringUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
// Inheritance
@WebServlet({"/api/expenses", "/api/expenses/*"})
public class ExpenseApiServlet extends BaseApiServlet {
    private static final ExpensesDao expenseDAO = new ExpensesDao();
    private static final ReportsDao reportDAO = new ReportsDao();

    // Data got from user structure
    static class ExpenseInput {
        Long reportId;
        Long categoryId;
        String title;
        BigDecimal amount;
        LocalDate expenseDate;
        String notes;
    }

    // Polymorphism
    // GET /api/expenses                                -> all expenses
    // GET /api/expenses?reportId=1&categoryId=2        -> filtered expenses
    // GET /api/expenses/{id}                           -> one expense
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long userId = currentUserId(req);
        Long id = pathId(req);
        if (id == null) {
            Long reportId = queryId(req, "reportId");
            Long categoryId = queryId(req, "categoryId");
            JsonUtil.write(resp, 200, expenseDAO.findAll(userId, reportId, categoryId));
            return;
        }
        Expense expense = expenseDAO.findBy(id, userId);
        if (expense == null) {
            throw new ApiException(404, "Expense not found");
        }
        JsonUtil.write(resp, 200, expense);
    }

    // POST /api/expenses
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long userId = currentUserId(req);
        if (pathId(req) != null) {
            throw new ApiException(404, "Not found");
        }
        ExpenseInput in = JsonUtil.readBody(req, ExpenseInput.class);
        Expense expense = validate(in);
        expense.setUserId(userId);
        requireOwnReport(expense.getReportId(), userId);
        long newId = expenseDAO.create(expense);
        JsonUtil.write(resp, 201, expenseDAO.findBy(newId, userId));
    }

    // PUT /api/expenses/{id}
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long userId = currentUserId(req);
        Long id = pathId(req);
        if (id == null) {
            throw new ApiException(400, "Expense id is required in the URL to Edit the same");
        }
        ExpenseInput in = JsonUtil.readBody(req, ExpenseInput.class);
        Expense expense = validate(in);
        expense.setId(id);
        expense.setUserId(userId);
        requireOwnReport(expense.getReportId(), userId);
        if (!expenseDAO.update(expense)) {
            throw new ApiException(404, "Expense not found");
        }
        JsonUtil.write(resp, 200, expenseDAO.findBy(id, userId));
    }

    // DELETE /api/expenses/{id}
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long userId = currentUserId(req);
        Long id = pathId(req);
        if (id == null) {
            throw new ApiException(400, "Expense ID required in URL");
        }
        if (!expenseDAO.delete(id, userId)) {
            throw new ApiException(404, "Expense not found");
        }
        JsonUtil.write(resp, 200, Map.of("message", "Expense deleted"));
    }

    private void requireOwnReport(Long reportId, long userId) {
        if (reportId != null && reportDAO.findBy(reportId, userId) == null) {
            throw new ApiException(400, "reportId does not exist");
        }
    }

    private Long queryId(HttpServletRequest req, String name) {
        String value = StringUtil.trim(req.getParameter(name));
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            throw new ApiException(400, name + " must be a number");
        }
    }

    // Builds an Expense from the input. The validation rules live in Expense's setters,
    // which throw IllegalArgumentException (BaseApiServlet turns that into a 400).
    private Expense validate(ExpenseInput in) {
        Expense e = new Expense();
        e.setTitle(in.title);
        e.setAmount(in.amount);
        e.setExpenseDate(in.expenseDate);
        e.setNotes(in.notes);
        e.setReportId(in.reportId);
        e.setCategoryId(in.categoryId);
        return e;
    }
}
