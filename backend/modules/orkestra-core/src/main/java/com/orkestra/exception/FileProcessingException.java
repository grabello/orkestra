package com.orkestra.exception;

public class FileProcessingException extends RuntimeException {
    private final String code;

    public FileProcessingException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
