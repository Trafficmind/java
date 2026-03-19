package com.trafficmind.sdk;

import java.util.List;

public final class UnauthorizedException extends TrafficmindException {
    public UnauthorizedException(String message, String responseBody, List<String> apiErrors, int attemptCount) {
        super(message, 401, responseBody, apiErrors, false, false, attemptCount, null);
    }
}
