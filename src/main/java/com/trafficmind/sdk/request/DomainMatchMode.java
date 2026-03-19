package com.trafficmind.sdk.request;

import java.util.Locale;

public enum DomainMatchMode {
    CONTAINS("contains"),
    STARTS_WITH("starts_with"),
    ENDS_WITH("ends_with"),
    NOT_EQUAL("not_equal"),
    EQUAL("equal"),
    STARTS_WITH_CASE_SENSITIVE("starts_with_case_sensitive"),
    ENDS_WITH_CASE_SENSITIVE("ends_with_case_sensitive"),
    CONTAINS_CASE_SENSITIVE("contains_case_sensitive");

    private final String value;

    DomainMatchMode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static DomainMatchMode fromValue(String raw) {
        if (raw == null) {
            return null;
        }
        String normalized = raw.toLowerCase(Locale.ROOT);
        for (DomainMatchMode v : values()) {
            if (v.value.equals(normalized)) {
                return v;
            }
        }
        throw new IllegalArgumentException("Unsupported DomainMatchMode: " + raw);
    }
}
