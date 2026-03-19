package com.trafficmind.sdk;

import com.trafficmind.sdk.dto.ApiResponse;
import com.trafficmind.sdk.dto.ResponseDomainRecord;
import com.trafficmind.sdk.dto.ResponseDomainId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class DomainsApiCrudTest {

    @Test
    void getDomainUsesCorrectPathAndParsesResponse() throws Exception {
        try (MockServer server = new MockServer()) {
            server.when("GET", "/public/v1/domains/z123", ex -> {
                MockServer.assertHeader(ex, "X-Access-User", "e");
                MockServer.assertHeader(ex, "X-Access-Key", "k");
                MockServer.writeJson(ex, 200,
                        "{\"status\":{\"code\":\"ok\"},\"payload\":{\"domain\":{\"id\":\"z123\",\"name\":\"example.com\"}}}");
            });

            TrafficmindClient client = TrafficmindClient.builder().email("e").apiKey("k").baseUrl(server.baseUri()).allowInsecureTransportForTesting(true).build();
            ApiResponse<ResponseDomainRecord> z = client.domains().get("z123");
            assertEquals("z123", z.getResult().getId());
            assertEquals("example.com", z.getResult().getName());
        }
    }

    @Test
    void deleteDomainUsesCorrectMethodAndPath() throws Exception {
        try (MockServer server = new MockServer()) {
            server.when("DELETE", "/public/v1/domains/z123", ex -> {
                MockServer.assertHeader(ex, "X-Access-User", "e");
                MockServer.assertHeader(ex, "X-Access-Key", "k");
                MockServer.writeJson(ex, 200, "{\"status\":{\"code\":\"ok\"},\"payload\":{\"domain\":{\"id\":\"z123\"}}}");
            });

            TrafficmindClient client = TrafficmindClient.builder().email("e").apiKey("k").baseUrl(server.baseUri()).allowInsecureTransportForTesting(true).build();
            ApiResponse<ResponseDomainId> deleted = client.domains().delete("z123");
            assertEquals("z123", deleted.getResult().getId());
        }
    }
}
