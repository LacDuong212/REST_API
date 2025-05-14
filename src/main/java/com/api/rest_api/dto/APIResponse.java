package com.api.rest_api.dto;

public class APIResponse {
    private String message;

    public APIResponse() {}

    public APIResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
