package com.orkestra.app.exception;

public class MissingTenantIdException extends RuntimeException {

    private final String code;

    public MissingTenantIdException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
