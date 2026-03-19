package com.trafficmind.sdk;

import java.util.Collections;
import java.util.List;

/**
 * Base runtime exception for SDK HTTP/serialization/API failures.
 */
public class TrafficmindException extends RuntimeException {
    private final int statusCode;
    private final String responseBody;
    private final List<String> apiErrors;
    private final boolean retryable;
    private final boolean transientError;
    private final int attemptCount;

    public TrafficmindException(String message) {
        this(message, 0, null, List.of(), false, false, 0, null);
    }

    public TrafficmindException(String message, int statusCode) {
        this(message, statusCode, null, List.of(), false, false, 0, null);
    }

    public TrafficmindException(String message, int statusCode, Throwable cause) {
        this(message, statusCode, null, List.of(), false, false, 0, cause);
    }

    public TrafficmindException(
            String message,
            int statusCode,
            String responseBody,
            List<String> apiErrors,
            boolean retryable,
            boolean transientError,
            int attemptCount,
            Throwable cause
    ) {
        super(sanitizeMessage(message, apiErrors), cause);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
        this.apiErrors = apiErrors == null ? List.of() : List.copyOf(apiErrors);
        this.retryable = retryable;
        this.transientError = transientError;
        this.attemptCount = attemptCount;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public List<String> getApiErrors() {
        return Collections.unmodifiableList(apiErrors);
    }

    /**
     * True when this error is safe and useful to retry (e.g. 429/5xx/network failures).
     */
    public boolean isRetryable() {
        return retryable;
    }

    /**
     * Alias for retryable/transient infrastructure errors.
     */
    public boolean isTransient() {
        return transientError;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public boolean isUnauthorized() {
        return statusCode == 401;
    }

    public boolean isForbidden() {
        return statusCode == 403;
    }

    public boolean isNotFound() {
        return statusCode == 404;
    }

    public boolean isRateLimited() {
        return statusCode == 429;
    }

    static TrafficmindException fromStatus(int statusCode, String message, String responseBody, List<String> apiErrors, int attemptCount) {
        boolean retryable = statusCode == 429 || statusCode >= 500;
        boolean transientError = retryable;
        return switch (statusCode) {
            case 401 -> new UnauthorizedException(message, responseBody, apiErrors, attemptCount);
            case 403 -> new ForbiddenException(message, responseBody, apiErrors, attemptCount);
            case 404 -> new NotFoundException(message, responseBody, apiErrors, attemptCount);
            case 429 -> new RateLimitedException(message, responseBody, apiErrors, attemptCount);
            default -> new TrafficmindException(message, statusCode, responseBody, apiErrors, retryable, transientError, attemptCount, null);
        };
    }

    private static String sanitizeMessage(String message, List<String> apiErrors) {
        String msg = message == null ? "" : message.trim();
        if (!msg.isEmpty()) {
            return msg;
        }
        if (apiErrors == null || apiErrors.isEmpty()) {
            return "";
        }
        return String.join("; ", apiErrors);
    }
}
