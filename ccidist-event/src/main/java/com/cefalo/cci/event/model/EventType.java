package com.cefalo.cci.event.model;

public enum EventType {
    CREATE("Create"), UPDATE("Update"), DELETE("Delete");

    private String typeName;

    private EventType(String type) {
        this.typeName = type;
    }

    @Override
    public String toString() {
        return typeName;
    }
}
