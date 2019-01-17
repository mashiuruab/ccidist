package com.cefalo.cci.enums;

public enum SortBy {
    CREATED("created"), UPDATED("updated");
    private String value;
    private SortBy(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
