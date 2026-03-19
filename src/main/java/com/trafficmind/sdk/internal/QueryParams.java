package com.trafficmind.sdk.internal;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

public final class QueryParams {
    private QueryParams() {}

    public static Map<String, String> of() {
        return new LinkedHashMap<>();
    }

    public static void putIfNotNull(Map<String, String> m, String key, Object value) {
        if (value == null) {
            return;
        }
        m.put(key, String.valueOf(value));
    }

    public static String toQueryString(Map<String, String> query) {
        if (query == null || query.isEmpty()) {
            return "";
        }
        StringJoiner sj = new StringJoiner("&");
        for (Map.Entry<String, String> e : query.entrySet()) {
            if (e.getKey() == null || e.getValue() == null) {
                continue;
            }
            sj.add(encode(e.getKey()) + "=" + encode(e.getValue()));
        }
        return sj.toString();
    }

    private static String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
