package com.orkestra.app.exception;

public class TenantAccessDeniedException extends RuntimeException{

    private final String code;

    public TenantAccessDeniedException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
