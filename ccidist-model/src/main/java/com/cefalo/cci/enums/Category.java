package com.cefalo.cci.enums;

public enum Category {
    UPDATED(1), INSERTED(2), DELETED(3);
    private int value;
    private Category(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
