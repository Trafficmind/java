package com.trafficmind.sdk;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

final class TrafficmindClientResilienceTest {

    @Test
    void enforcesHttpsByDefault() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new TrafficmindClient("e", "k", "http://api.trafficmind.com")
        );
        assertTrue(ex.getMessage().contains("https"));
    }

    @Test
    void rejectsNonPublicApiPath() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new TrafficmindClient("e", "k", "https://api.trafficmind.com/internal/v1/")
        );
        assertTrue(ex.getMessage().contains("/public/v1"));
    }

    @Test
    void retriesOn429AndRespectsRetryAfter() throws Exception {
        try (MockServer server = new MockServer()) {
            AtomicInteger calls = new AtomicInteger();
            server.when("GET", "/public/v1/domains", ex -> {
                int n = calls.incrementAndGet();
                if (n == 1) {
                    ex.getResponseHeaders().add("Retry-After", "0");
                    MockServer.writeJson(ex, 429, "{\"message\":\"rate limited\"}");
                    return;
                }
                MockServer.writeJson(ex, 200, "{\"status\":{\"code\":\"ok\"},\"payload\":{\"items\":[]}}");
            });

            TrafficmindClient client = TrafficmindClient.builder()
                    .email("e")
                    .apiKey("k")
                    .baseUrl(server.baseUri())
                    .allowInsecureTransportForTesting(true)
                    .withRetries(2, Duration.ofMillis(1))
                    .build();

            assertEquals(0, client.domains().list(null).getResult().size());
            assertEquals(2, calls.get());
        }
    }

    @Test
    void retriesOn429AndRespectsRetryAfterHttpDate() throws Exception {
        try (MockServer server = new MockServer()) {
            AtomicInteger calls = new AtomicInteger();
            server.when("GET", "/public/v1/domains", ex -> {
                int n = calls.incrementAndGet();
                if (n == 1) {
                    String at = OffsetDateTime.now().plusSeconds(1).format(DateTimeFormatter.RFC_1123_DATE_TIME);
                    ex.getResponseHeaders().add("Retry-After", at);
                    MockServer.writeJson(ex, 429, "{\"message\":\"rate limited\"}");
                    return;
                }
                MockServer.writeJson(ex, 200, "{\"status\":{\"code\":\"ok\"},\"payload\":{\"items\":[]}}");
            });

            TrafficmindClient client = TrafficmindClient.builder()
                    .email("e")
                    .apiKey("k")
                    .baseUrl(server.baseUri())
                    .allowInsecureTransportForTesting(true)
                    .withRetries(1, Duration.ofMillis(1))
                    .build();

            assertEquals(0, client.domains().list(null).getResult().size());
            assertEquals(2, calls.get());
        }
    }

    @Test
    void parseRetryAfterInvalidReturnsNull() {
        assertNull(TrafficmindClient.parseRetryAfter("invalid"));
    }

    @Test
    void rejectsInvalidTimeoutConfiguration() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> TrafficmindClient.builder()
                        .email("e")
                        .apiKey("k")
                        .requestTimeout(Duration.ZERO)
                        .build()
        );
        assertTrue(ex.getMessage().contains("requestTimeout"));
    }

    @Test
    void doesNotRetryOn400() throws Exception {
        try (MockServer server = new MockServer()) {
            AtomicInteger calls = new AtomicInteger();
            server.when("GET", "/public/v1/domains", ex -> {
                calls.incrementAndGet();
                MockServer.writeJson(ex, 400, "{\"message\":\"bad request\"}");
            });

            TrafficmindClient client = TrafficmindClient.builder()
                    .email("e")
                    .apiKey("k")
                    .baseUrl(server.baseUri())
                    .allowInsecureTransportForTesting(true)
                    .withRetries(3, Duration.ofMillis(1))
                    .build();

            TrafficmindException ex = assertThrows(TrafficmindException.class, () -> client.domains().list(null));
            assertEquals(400, ex.getStatusCode());
            assertEquals(1, calls.get());
            assertEquals(1, ex.getAttemptCount());
        }
    }

    @Test
    void includesAttemptCountOnRetryExhaustion() throws Exception {
        try (MockServer server = new MockServer()) {
            AtomicInteger calls = new AtomicInteger();
            server.when("GET", "/public/v1/domains", ex -> {
                calls.incrementAndGet();
                MockServer.writeJson(ex, 503, "{\"message\":\"unavailable\"}");
            });

            TrafficmindClient client = TrafficmindClient.builder()
                    .email("e")
                    .apiKey("k")
                    .baseUrl(server.baseUri())
                    .allowInsecureTransportForTesting(true)
                    .withRetries(1, Duration.ofMillis(1))
                    .build();

            TrafficmindException ex = assertThrows(TrafficmindException.class, () -> client.domains().list(null));
            assertEquals(2, ex.getAttemptCount());
            assertTrue(ex.getMessage().contains("after 2 attempts"));
            assertEquals(2, calls.get());
        }
    }

    @Test
    void doesNotRetryPostWithoutIdempotencyKey() throws Exception {
        try (MockServer server = new MockServer()) {
            AtomicInteger calls = new AtomicInteger();
            server.when("POST", "/public/v1/domains", ex -> {
                calls.incrementAndGet();
                MockServer.writeJson(ex, 503, "{\"message\":\"unavailable\"}");
            });

            TrafficmindClient client = TrafficmindClient.builder()
                    .email("e")
                    .apiKey("k")
                    .baseUrl(server.baseUri())
                    .allowInsecureTransportForTesting(true)
                    .withRetries(2, Duration.ofMillis(1))
                    .build();

            com.trafficmind.sdk.dto.CreateDomainRequest request = new com.trafficmind.sdk.dto.CreateDomainRequest();
            request.setName("example.com");

            TrafficmindException ex = assertThrows(TrafficmindException.class, () -> client.domains().create(request));
            assertEquals(503, ex.getStatusCode());
            assertEquals(1, calls.get());
            assertEquals(1, ex.getAttemptCount());
        }
    }

    @Test
    void retriesPostWhenIdempotencyKeyConfigured() throws Exception {
        try (MockServer server = new MockServer()) {
            AtomicInteger calls = new AtomicInteger();
            server.when("POST", "/public/v1/domains", ex -> {
                int n = calls.incrementAndGet();
                if (n == 1) {
                    MockServer.writeJson(ex, 503, "{\"message\":\"unavailable\"}");
                    return;
                }
                MockServer.writeJson(ex, 201, "{\"status\":{\"code\":\"created\"},\"payload\":{\"domain\":{\"id\":\"z1\",\"name\":\"example.com\"}}}");
            });

            TrafficmindClient client = TrafficmindClient.builder()
                    .email("e")
                    .apiKey("k")
                    .baseUrl(server.baseUri())
                    .allowInsecureTransportForTesting(true)
                    .withRetries(2, Duration.ofMillis(1))
                    .withIdempotencyKey("idem-1")
                    .build();

            com.trafficmind.sdk.dto.CreateDomainRequest request = new com.trafficmind.sdk.dto.CreateDomainRequest();
            request.setName("example.com");
            assertEquals("z1", client.domains().create(request).getResult().getId());
            assertEquals(2, calls.get());
        }
    }

    @Test
    void redactsPasswordInToString() {
        var dto = new com.trafficmind.sdk.dto.CDNUserResponse();
        dto.setPassword("secret");
        dto.setUsername("u");

        String dump = dto.toString();
        assertFalse(dump.contains("secret"));
        assertTrue(dump.contains("[REDACTED]"));
    }

    @Test
    void failsWhenResponseExceedsConfiguredLimit() throws Exception {
        try (MockServer server = new MockServer()) {
            server.when("GET", "/public/v1/domains", ex ->
                    MockServer.writeJson(ex, 200, "{\"status\":{\"code\":\"ok\"},\"payload\":{\"items\":[{\"id\":\"" + "x".repeat(1000) + "\"}]}}"));

            TrafficmindClient client = TrafficmindClient.builder()
                    .email("e")
                    .apiKey("k")
                    .baseUrl(server.baseUri())
                    .allowInsecureTransportForTesting(true)
                    .withMaxResponseBytes(64)
                    .build();

            TrafficmindException ex = assertThrows(TrafficmindException.class, () -> client.domains().list(null));
            assertTrue(ex.getMessage().contains("exceeds configured limit"));
        }
    }
}
