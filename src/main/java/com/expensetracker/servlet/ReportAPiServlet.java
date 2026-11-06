package com.expensetracker.servlet;

import com.expensetracker.dao.ExpensesDao;
import com.expensetracker.dao.ReportsDao;
import com.expensetracker.model.Report;
import com.expensetracker.util.ApiException;
import com.expensetracker.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@WebServlet({"/api/reports", "/api/reports/*"})
public class ReportAPiServlet extends BaseApiServlet {
    private static final ReportsDao reportDAO = new ReportsDao();
    private static final ExpensesDao expenseDAO = new ExpensesDao();

    // Data got from user structure
    static class ReportInput {
        String name;
        String description;
        LocalDate startDate;
        LocalDate endDate;
        BigDecimal budget;
    }

    // GET /api/reports      -> all reports with totals
    // GET /api/reports/{id} -> one report
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long userId = currentUserId(req);
        Long id = pathId(req);
        if(id == null) {
            JsonUtil.write(resp, 200, reportDAO.findAll(userId));
            return;
        }
        Report report = reportDAO.findBy(id, userId);
        if (report == null) {
            throw new ApiException(404, "Report not found");
        }
        report.setExpenses(expenseDAO.findAll(userId, id, null));
        JsonUtil.write(resp, 200, report);
    }

    // POST /api/reports
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long userId = currentUserId(req);
        if (pathId(req) != null) {
            throw new ApiException(404, "Not found");
        }
        ReportInput in = JsonUtil.readBody(req, ReportInput.class);
        Report report = validate(in);
        report.setUserId(userId);
        long newId = reportDAO.create(report);
        JsonUtil.write(resp, 201, reportDAO.findBy(newId, userId));
    }

    // PUT /api/reports/{id}
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long userId = currentUserId(req);
        Long id = pathId(req);
        if(id == null) {
            throw new ApiException(400, "Report id is required in the URL to Edit the same");
        }
        ReportInput in = JsonUtil.readBody(req, ReportInput.class);
        Report report = validate(in);
        report.setId(id);
        report.setUserId(userId);
        if(!reportDAO.update(report)){
            throw new ApiException(404, "Report not found");
        }
        JsonUtil.write(resp, 200, reportDAO.findBy(id, userId));
    }

    // DELETE /api/reports/{id}                      -> expenses stay, unassigned
    // DELETE /api/reports/{id}?deleteExpenses=true  -> expenses are deleted too
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long userId = currentUserId(req);
        Long id = pathId(req);
        if(id == null) {
            throw new ApiException(400, "Report ID required in URL");
        }
        boolean isDelete = "true".equalsIgnoreCase(req.getParameter("deleteExpenses"));
        if(!reportDAO.delete(id, userId, isDelete)) {
            throw new ApiException(404, "Report not found");
        }
        JsonUtil.write(resp, 200, Map.of("message", "Report deleted"));
    }

    private Report validate(ReportInput in) {
        Report r = new Report();
        r.setName(in.name);
        r.setDescription(in.description);
        r.setStartDate(in.startDate);
        r.setEndDate(in.endDate);
        r.setBudget(in.budget);
        return r;
    }
}
