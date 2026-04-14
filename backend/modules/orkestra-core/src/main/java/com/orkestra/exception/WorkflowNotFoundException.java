package com.orkestra.exception;

public class WorkflowNotFoundException extends RuntimeException {

    private final String code;

    public WorkflowNotFoundException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
