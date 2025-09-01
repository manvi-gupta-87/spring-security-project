package com.example.demo.errors;

import java.time.Instant;
import java.util.Map;

public class ApiError {
    public final Instant timestamp = Instant.now();
    public final int status;
    public final String error;   // short label e.g. "Unauthorized"
    public final String message; // human-readable or summary
    public final String path;    // request path
    public final Map<String,Object> details; // optional field map

    public ApiError(int status, String error, String message, String path) {
        this(status, error, message, path, null);
    }

    public ApiError(int status, String error, String message, String path, Map<String,Object> details) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.details = details;
    }
}
