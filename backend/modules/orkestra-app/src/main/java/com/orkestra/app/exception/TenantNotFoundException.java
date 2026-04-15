package com.orkestra.app.exception;

public class TenantNotFoundException extends RuntimeException {
    private final String code;

    public TenantNotFoundException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
