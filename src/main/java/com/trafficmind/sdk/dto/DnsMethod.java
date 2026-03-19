package com.trafficmind.sdk.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

public enum DnsMethod {
    AUTO("auto"),
    FILE("file"),
    MANUAL("manual"),
    SOURCE("source");

    private final String value;

    DnsMethod(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static DnsMethod fromValue(String raw) {
        if (raw == null) {
            return null;
        }
        String normalized = raw.toLowerCase(Locale.ROOT);
        for (DnsMethod v : values()) {
            if (v.value.equals(normalized)) {
                return v;
            }
        }
        throw new IllegalArgumentException("Unsupported DnsMethod: " + raw);
    }
}
