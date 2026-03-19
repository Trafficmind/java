package com.trafficmind.sdk;

import java.util.List;

public final class ForbiddenException extends TrafficmindException {
    public ForbiddenException(String message, String responseBody, List<String> apiErrors, int attemptCount) {
        super(message, 403, responseBody, apiErrors, false, false, attemptCount, null);
    }
}
