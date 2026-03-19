package com.trafficmind.sdk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class TrafficmindClientAuthHeadersTest {

    @Test
    void includesAuthHeaders() throws Exception {
        try (MockServer server = new MockServer()) {
            server.when("GET", "/public/v1/domains", ex -> {
                MockServer.assertHeader(ex, "X-Access-User", "test@example.com");
                MockServer.assertHeader(ex, "X-Access-Key", "KEY");
                MockServer.writeJson(ex, 200, "{\"status\":{\"code\":\"ok\"},\"payload\":{\"items\":[]}}");
            });

            TrafficmindClient client = TrafficmindClient.builder().email("test@example.com").apiKey("KEY").baseUrl(server.baseUri()).allowInsecureTransportForTesting(true).build();

            assertNotNull(client.domains().list(null));
            assertEquals(0, client.domains().list(null).getResult().size());
        }
    }
}
