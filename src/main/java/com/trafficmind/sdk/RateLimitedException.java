package com.trafficmind.sdk;

import java.util.List;

public final class RateLimitedException extends TrafficmindException {
    public RateLimitedException(String message, String responseBody, List<String> apiErrors, int attemptCount) {
        super(message, 429, responseBody, apiErrors, true, true, attemptCount, null);
    }
}
