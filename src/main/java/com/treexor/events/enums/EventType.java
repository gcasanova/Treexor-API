package com.treexor.events.enums;

public enum EventType {
    LOGIN("login"), LOGOUT("logout"), REGISTRATION("registration"), PURCHASE("purchase");

    private String value;

    private EventType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static EventType findByValue(String value) {
        for (EventType type : EventType.values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return null;
    }
}
