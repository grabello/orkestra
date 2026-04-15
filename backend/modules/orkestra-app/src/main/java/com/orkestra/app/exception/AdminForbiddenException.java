package com.orkestra.app.exception;

public class AdminForbiddenException extends RuntimeException{

    private final String code;

    public AdminForbiddenException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
