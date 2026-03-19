package com.trafficmind.sdk;

import java.util.List;

public final class NotFoundException extends TrafficmindException {
    public NotFoundException(String message, String responseBody, List<String> apiErrors, int attemptCount) {
        super(message, 404, responseBody, apiErrors, false, false, attemptCount, null);
    }
}
