package com.trafficmind.sdk;

import com.trafficmind.sdk.request.DomainListRequest;
import com.trafficmind.sdk.request.DomainMatchMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class DomainsQueryParamsTest {

    @Test
    void sendsQueryParams() throws Exception {
        try (MockServer server = new MockServer()) {
            server.when("GET", "/public/v1/domains", ex -> {
                String qs = ex.getRequestURI().getRawQuery();
                assertNotNull(qs);
                assertTrue(qs.contains("query=example.com"));
                assertTrue(qs.contains("match_mode=equal"));
                assertTrue(qs.contains("page=2"));
                assertTrue(qs.contains("page_size=50"));

                MockServer.writeJson(ex, 200, "{\"status\":{\"code\":\"ok\"},\"payload\":{\"items\":[]}}");
            });

            TrafficmindClient client = TrafficmindClient.builder().email("e").apiKey("k").baseUrl(server.baseUri()).allowInsecureTransportForTesting(true).build();
            client.domains().list(DomainListRequest.create().query("example.com").matchMode(DomainMatchMode.EQUAL).page(2).pageSize(50));
        }
    }

    @Test
    void rejectsUnsupportedMatchMode() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> DomainListRequest.create().matchMode("unsupported"));
        assertTrue(ex.getMessage().contains("Unsupported DomainMatchMode"));
    }

    @Test
    void listAllWalksThroughPages() throws Exception {
        try (MockServer server = new MockServer()) {
            server.when("GET", "/public/v1/domains", ex -> {
                String qs = ex.getRequestURI().getRawQuery();
                if (qs != null && qs.contains("page=1")) {
                    MockServer.writeJson(ex, 200, "{\"status\":{\"code\":\"ok\"},\"payload\":{\"items\":[{\"id\":\"z1\"}],\"pagination\":{\"page\":1,\"page_size\":1,\"total\":2,\"items\":1}}}");
                } else {
                    MockServer.writeJson(ex, 200, "{\"status\":{\"code\":\"ok\"},\"payload\":{\"items\":[{\"id\":\"z2\"}],\"pagination\":{\"page\":2,\"page_size\":1,\"total\":2,\"items\":1}}}");
                }
            });

            TrafficmindClient client = TrafficmindClient.builder().email("e").apiKey("k").baseUrl(server.baseUri()).allowInsecureTransportForTesting(true).build();
            assertEquals(2, client.domains().listAll(DomainListRequest.create().pageSize(1)).getResult().size());
        }
    }

    @Test
    void listAllHonorsMaxPagesLimit() throws Exception {
        try (MockServer server = new MockServer()) {
            server.when("GET", "/public/v1/domains", ex -> {
                String qs = ex.getRequestURI().getRawQuery();
                if (qs != null && qs.contains("page=1")) {
                    MockServer.writeJson(ex, 200, "{\"status\":{\"code\":\"ok\"},\"payload\":{\"items\":[{\"id\":\"z1\"}],\"pagination\":{\"page\":1,\"page_size\":1,\"total\":3,\"items\":1}}}");
                } else if (qs != null && qs.contains("page=2")) {
                    MockServer.writeJson(ex, 200, "{\"status\":{\"code\":\"ok\"},\"payload\":{\"items\":[{\"id\":\"z2\"}],\"pagination\":{\"page\":2,\"page_size\":1,\"total\":3,\"items\":1}}}");
                } else {
                    MockServer.writeJson(ex, 200, "{\"status\":{\"code\":\"ok\"},\"payload\":{\"items\":[{\"id\":\"z3\"}],\"pagination\":{\"page\":3,\"page_size\":1,\"total\":3,\"items\":1}}}");
                }
            });

            TrafficmindClient client = TrafficmindClient.builder().email("e").apiKey("k").baseUrl(server.baseUri()).allowInsecureTransportForTesting(true).build();
            assertEquals(2, client.domains().listAll(DomainListRequest.create().pageSize(1), 2).getResult().size());
        }
    }

    @Test
    void listAllUsesPageSizeWhenProvided() throws Exception {
        try (MockServer server = new MockServer()) {
            server.when("GET", "/public/v1/domains", ex -> {
                String qs = ex.getRequestURI().getRawQuery();
                assertNotNull(qs);
                assertTrue(qs.contains("page_size=25"), "query: " + qs);
                if (qs.contains("page=1")) {
                    MockServer.writeJson(ex, 200, "{\"status\":{\"code\":\"ok\"},\"payload\":{\"items\":[{\"id\":\"z1\"}],\"pagination\":{\"page\":1,\"page_size\":25,\"total\":26,\"items\":1}}}");
                } else {
                    MockServer.writeJson(ex, 200, "{\"status\":{\"code\":\"ok\"},\"payload\":{\"items\":[{\"id\":\"z2\"}],\"pagination\":{\"page\":2,\"page_size\":25,\"total\":26,\"items\":1}}}");
                }
            });

            TrafficmindClient client = TrafficmindClient.builder().email("e").apiKey("k").baseUrl(server.baseUri()).allowInsecureTransportForTesting(true).build();
            assertEquals(2, client.domains().listAll(DomainListRequest.create().pageSize(25)).getResult().size());
        }
    }

    @Test
    void streamAllUsesPageSizeWhenProvided() throws Exception {
        try (MockServer server = new MockServer()) {
            server.when("GET", "/public/v1/domains", ex -> {
                String qs = ex.getRequestURI().getRawQuery();
                assertNotNull(qs);
                assertTrue(qs.contains("page_size=25"), "query: " + qs);
                if (qs.contains("page=1")) {
                    MockServer.writeJson(ex, 200, "{\"status\":{\"code\":\"ok\"},\"payload\":{\"items\":[{\"id\":\"z1\"}],\"pagination\":{\"page\":1,\"page_size\":25,\"total\":26,\"items\":1}}}");
                } else {
                    MockServer.writeJson(ex, 200, "{\"status\":{\"code\":\"ok\"},\"payload\":{\"items\":[{\"id\":\"z2\"}],\"pagination\":{\"page\":2,\"page_size\":25,\"total\":26,\"items\":1}}}");
                }
            });

            TrafficmindClient client = TrafficmindClient.builder().email("e").apiKey("k").baseUrl(server.baseUri()).allowInsecureTransportForTesting(true).build();
            assertEquals(2, client.domains().streamAll(DomainListRequest.create().pageSize(25)).toList().size());
        }
    }

    @Test
    void listAllStopsWhenThreadInterrupted() throws Exception {
        try (MockServer server = new MockServer()) {
            server.when("GET", "/public/v1/domains", ex ->
                    MockServer.writeJson(ex, 200, "{\"status\":{\"code\":\"ok\"},\"payload\":{\"items\":[],\"pagination\":{\"page\":1,\"page_size\":1,\"total\":2,\"items\":0}}}"));

            TrafficmindClient client = TrafficmindClient.builder().email("e").apiKey("k").baseUrl(server.baseUri()).allowInsecureTransportForTesting(true).build();
            Thread.currentThread().interrupt();
            try {
                assertThrows(IllegalStateException.class, () -> client.domains().listAll(DomainListRequest.create(), 2));
            } finally {
                Thread.interrupted();
            }
        }
    }
}
