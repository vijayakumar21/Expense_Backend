package com.expensetracker.servlet;

import com.expensetracker.util.ApiException;
import com.expensetracker.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

// parent of all API servlets
public abstract class BaseApiServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //what if not set
        req.setCharacterEncoding("UTF-8");
        try {
            super.service(req, resp);
        } catch (ApiException e) {
            log("Error on " + req.getMethod() + " " + req.getRequestURI(), e);
            JsonUtil.write(resp, e.getStatus(), e.toBody());
        } catch (IllegalArgumentException e) {
            // validation failure raised by a model setter
            JsonUtil.write(resp, 400, Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log("Error on " + req.getMethod() + " " + req.getRequestURI(), e);
            JsonUtil.write(resp, 500, Map.of("error", "Internal Server Error"));
        }
    }
    protected Long pathId(HttpServletRequest req)  {
        String path = req.getPathInfo();
        if (path == null || path.equals("/")) {
            return null;
        }
        try {
            return Long.valueOf(path.substring(1));
        } catch (NumberFormatException e) {
            // when tried to convert a string to number
            throw new ApiException(404, "Not found");
        }
    }

    protected long currentUserId(HttpServletRequest req) {
        String header = req.getHeader("X-User-Id");
        if (header == null || header.isBlank()) {
            throw new ApiException(401, "X-User-Id header is required");
        }
        try {
            long userId = Long.parseLong(header.trim());
            if (userId <= 0) {
                throw new NumberFormatException();
            }
            return userId;
        } catch (NumberFormatException e) {
            throw new ApiException(401, "X-User-Id header must be a positive number");
        }
    }
}
