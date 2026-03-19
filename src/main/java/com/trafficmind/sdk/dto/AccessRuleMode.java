package com.trafficmind.sdk.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

public enum AccessRuleMode {
    CHALLENGE("challenge"),
    BLOCK("block"),
    ALLOW("allow");

    private final String value;

    AccessRuleMode(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static AccessRuleMode fromValue(String raw) {
        if (raw == null) {
            return null;
        }
        String normalized = raw.toLowerCase(Locale.ROOT);
        for (AccessRuleMode v : values()) {
            if (v.value.equals(normalized)) {
                return v;
            }
        }
        throw new IllegalArgumentException("Unsupported AccessRuleMode: " + raw);
    }
}
