package com.trafficmind.sdk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class TrafficmindClientErrorHandlingTest {

    @Test
    void throwsWhenApiReturnsErrorEnvelope() throws Exception {
        try (MockServer server = new MockServer()) {
            server.when("GET", "/public/v1/domains", ex -> {
                MockServer.writeJson(ex, 400,
                        "{\"status\":{\"code\":\"validation_error\",\"message\":\"bad api key\"},\"error\":{\"message\":\"bad api key\"}}");
            });

            TrafficmindClient client = TrafficmindClient.builder()
                    .email("user@example.com")
                    .apiKey("SECRETKEY")
                    .baseUrl(server.baseUri())
                    .allowInsecureTransportForTesting(true)
                    .build();
            TrafficmindException ex = assertThrows(TrafficmindException.class, () -> client.domains().list(null));
            assertEquals(400, ex.getStatusCode());
            assertTrue(ex.getMessage().contains("bad api key"));
            assertEquals(1, ex.getApiErrors().size());
        }
    }

    @Test
    void throwsTypedExceptionFor401() throws Exception {
        try (MockServer server = new MockServer()) {
            server.when("GET", "/public/v1/domains", ex -> MockServer.writeJson(ex, 401, "{\"error\":{\"message\":\"unauthorized\"},\"status\":{\"code\":\"unauthorized\"}}"));

            TrafficmindClient client = TrafficmindClient.builder()
                    .email("e")
                    .apiKey("k")
                    .baseUrl(server.baseUri())
                    .allowInsecureTransportForTesting(true)
                    .withRetries(0, null)
                    .build();

            TrafficmindException ex = assertThrows(TrafficmindException.class, () -> client.domains().list(null));
            assertInstanceOf(UnauthorizedException.class, ex);
            assertEquals(401, ex.getStatusCode());
            assertFalse(ex.isRetryable());
        }
    }

    @Test
    void throwsOnInvalidJsonEvenWhenStatusIs200() throws Exception {
        try (MockServer server = new MockServer()) {
            server.when("GET", "/public/v1/domains", ex -> MockServer.writeJson(ex, 200, "not-json"));

            TrafficmindClient client = TrafficmindClient.builder()
                    .email("user@example.com")
                    .apiKey("SECRETKEY")
                    .baseUrl(server.baseUri())
                    .allowInsecureTransportForTesting(true)
                    .build();
            TrafficmindException ex = assertThrows(TrafficmindException.class, () -> client.domains().list(null));
            assertEquals(200, ex.getStatusCode());
            assertTrue(ex.getMessage().toLowerCase().contains("decode"));
        }
    }

    @Test
    void sanitizesCredentialValuesInErrorMessages() throws Exception {
        try (MockServer server = new MockServer()) {
            server.when("GET", "/public/v1/domains", ex ->
                    MockServer.writeJson(ex, 500, "{\"error\":{\"message\":\"bad key k for e@example.com\"},\"status\":{\"code\":\"internal_error\"}}"));

            TrafficmindClient client = TrafficmindClient.builder()
                    .email("e@example.com")
                    .apiKey("k")
                    .baseUrl(server.baseUri())
                    .allowInsecureTransportForTesting(true)
                    .withRetries(0, null)
                    .build();
            TrafficmindException ex = assertThrows(TrafficmindException.class, () -> client.domains().list(null));
            assertFalse(ex.getMessage().contains("e@example.com"));
            assertFalse(ex.getMessage().contains(" key k "));
            assertTrue(ex.getMessage().contains("[REDACTED]"));
        }
    }
}
