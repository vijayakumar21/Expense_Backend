package com.expensetracker.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class ApiException extends RuntimeException {
    private final int status;
    private final Map<String, String> fields;

    public ApiException(int status, String msg) {
        this(status, msg, null);
    }
    public ApiException(int status, String message, Map<String, String> fields) {
        super(message);
        this.status = status;
        this.fields = fields;
    }

    public int getStatus() { return status; }

    public Map<String, Object> toBody() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", getMessage());
        if (fields != null && !fields.isEmpty()) body.put("fields", fields);
        return body;
    }

    public static ApiException notFound(String what) {
        return new ApiException(404, what + " not found");
    }
}
