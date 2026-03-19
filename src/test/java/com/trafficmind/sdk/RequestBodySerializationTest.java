package com.trafficmind.sdk;

import com.trafficmind.sdk.dto.CreateDomainRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class RequestBodySerializationTest {

    @Test
    void serializesRequestBodyWithExpectedFields() throws Exception {
        try (MockServer server = new MockServer()) {
            server.when("POST", "/public/v1/domains", ex -> {
                String body = MockServer.readBody(ex);
                assertTrue(body.contains("\"name\":\"example.com\""), "body: " + body);
                MockServer.writeJson(ex, 201,
                        "{\"status\":{\"code\":\"created\"},\"payload\":{\"domain\":{\"id\":\"z1\",\"name\":\"example.com\"}}}");
            });

            TrafficmindClient client = TrafficmindClient.builder().email("e").apiKey("k").baseUrl(server.baseUri()).allowInsecureTransportForTesting(true).build();

            CreateDomainRequest req = new CreateDomainRequest();
            req.setName("example.com");

            assertEquals("z1", client.domains().create(req).getResult().getId());
        }
    }
}
