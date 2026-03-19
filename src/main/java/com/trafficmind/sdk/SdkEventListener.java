package com.trafficmind.sdk;

import java.time.Duration;

/**
 * Hook interface for SDK observability integrations.
 *
 * <p>Implementations must be thread-safe because SDK requests can be executed concurrently
 * by multiple threads using the same {@link TrafficmindClient} instance.</p>
 *
 * <p>All callbacks are best-effort and must not throw; implementations should handle their own
 * failures to avoid affecting request execution flow.</p>
 */
public interface SdkEventListener {
    SdkEventListener NOOP = new SdkEventListener() {};

    /**
     * Invoked before an HTTP attempt is sent.
     */
    default void onRequest(String url, String method, int attempt) {
    }

    /**
     * Invoked on successful HTTP responses (2xx with successful envelope).
     */
    default void onResponse(String url, int statusCode, int attempt, long durationMs, long timestampMs) {
    }

    /**
     * Invoked when an attempt ends with SDK exception.
     */
    default void onError(String url, TrafficmindException exception, int attempt, long durationMs, long timestampMs) {
    }

    /**
     * Invoked before retrying a failed request attempt.
     * {@code attempt} is the failed attempt number, and {@code reason} is a sanitized summary.
     */
    default void onRetry(String url, String method, int attempt, Duration delay, String reason) {
    }
}
