package com.fintrack.response;

import java.time.LocalDateTime;

public class ApiResponse<T> {
    private String timestamp;
    private int status;
    private String message;
    private T data;

    public ApiResponse() {}

    public ApiResponse(String timestamp, int status, String message, T data) {
        this.timestamp = timestamp;
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public ApiResponse(int status, String message, T data) {
        this.timestamp = LocalDateTime.now().toString();
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}
