package com.smartcampus.model;

public class ErrorMessage {
    private int status;
    private String code;
    private String message;

    public ErrorMessage() {}

    public ErrorMessage(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    // Getters and Setters
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}