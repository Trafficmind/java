package com.trafficmind.sdk.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

public enum AccessRuleTarget {
    IP("ip"),
    COUNTRY("country");

    private final String value;

    AccessRuleTarget(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static AccessRuleTarget fromValue(String raw) {
        if (raw == null) {
            return null;
        }
        String normalized = raw.toLowerCase(Locale.ROOT);
        for (AccessRuleTarget v : values()) {
            if (v.value.equals(normalized)) {
                return v;
            }
        }
        throw new IllegalArgumentException("Unsupported AccessRuleTarget: " + raw);
    }
}
