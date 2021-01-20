package com.ilkom.network;

public class BaseResponse {

    private boolean success;
    private String message;
    private String filename;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public String getFilename() {
        return filename;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}