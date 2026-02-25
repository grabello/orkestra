package com.orkestra.dsl.model;

public enum DslType {
    NOOP("noop"),
    HTTP("http");

    private String value;

    DslType(String value) {
        this.value = value;
    }

    public static DslType fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (DslType type : DslType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }

}
