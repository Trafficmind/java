package com.trafficmind.sdk;

import com.trafficmind.sdk.dto.*;
import com.trafficmind.sdk.request.DnsListRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

final class DnsApiTest {

    @Test
    void listDnsRecordsSendsQueryParamsAndParsesList() throws Exception {
        try (MockServer server = new MockServer()) {
            server.when("GET", "/public/v1/domains/z1/records", ex -> {
                MockServer.assertHeader(ex, "X-Access-User", "e");
                MockServer.assertHeader(ex, "X-Access-Key", "k");

                assertEquals("page=2&page_size=10&query=all", ex.getRequestURI().getQuery());

                String json = "{" +
                        "\"status\":{\"code\":\"ok\"}," +
                        "\"payload\":{\"items\":[{\"id\":\"r1\",\"type\":\"A\",\"name\":\"a.example.com\",\"content\":\"1.1.1.1\"}],\"pagination\":{\"page\":2,\"page_size\":10,\"items\":1,\"total\":1},\"search_query\":\"all\"}" +
                        "}";
                MockServer.writeJson(ex, 200, json);
            });

            TrafficmindClient client = TrafficmindClient.builder().email("e").apiKey("k").baseUrl(server.baseUri()).allowInsecureTransportForTesting(true).build();
            ApiResponse<List<DomainDNSRecord>> records = client.dns().list(DnsListRequest.create().query("all").page(2).pageSize(10), "z1");

            assertEquals(1, records.getResult().size());
            assertEquals("r1", records.getResult().get(0).getId());
            assertEquals("A", records.getResult().get(0).getType());
            assertEquals("all", records.getPayload().path("search_query").asText());
        }
    }

    @Test
    void listDnsRecordsWithPayloadParsesSearchQueryAsTypedField() throws Exception {
        try (MockServer server = new MockServer()) {
            server.when("GET", "/public/v1/domains/z1/records", ex -> {
                String json = "{" +
                        "\"status\":{\"code\":\"ok\"}," +
                        "\"meta\":{\"request_id\":\"req-dns-payload\",\"timestamp\":\"2026-03-16T10:20:33Z\"}," +
                        "\"payload\":{\"items\":[{\"id\":\"r1\",\"type\":\"A\",\"name\":\"a.example.com\",\"content\":\"1.1.1.1\"}],\"pagination\":{\"page\":2,\"page_size\":10,\"items\":1,\"total\":1},\"search_query\":\"api.example.com\"}" +
                        "}";
                MockServer.writeJson(ex, 200, json);
            });

            TrafficmindClient client = TrafficmindClient.builder().email("e").apiKey("k").baseUrl(server.baseUri()).allowInsecureTransportForTesting(true).build();
            ApiResponse<DomainRecordListPayload> response = client.dns().listWithPayload(DnsListRequest.create().query("api.example.com").page(2).pageSize(10), "z1");

            assertNotNull(response.getResult());
            assertEquals("api.example.com", response.getResult().getSearchQuery());
            assertNotNull(response.getResult().getItems());
            assertEquals(1, response.getResult().getItems().size());
            assertEquals("r1", response.getResult().getItems().get(0).getId());
            assertNotNull(response.getResult().getPagination());
            assertEquals(2, response.getResult().getPagination().getPage());
            assertEquals(10, response.getResult().getPagination().getPageSize());
            assertEquals(1, response.getResult().getPagination().getTotalCount());
            assertNotNull(response.getMeta());
            assertEquals("req-dns-payload", response.getMeta().getRequestId());
        }
    }

    @Test
    void listDnsRecordsWithInfoParsesResultInfo() throws Exception {
        try (MockServer server = new MockServer()) {
            server.when("GET", "/public/v1/domains/z1/records", ex -> {
                String json = "{" +
                        "\"status\":{\"code\":\"ok\",\"message\":\"dns list loaded\"}," +
                        "\"meta\":{\"request_id\":\"req-dns-1\",\"timestamp\":\"2026-03-13T10:20:33Z\"}," +
                        "\"payload\":{\"items\":[{\"id\":\"r1\",\"type\":\"A\",\"name\":\"a.example.com\",\"content\":\"1.1.1.1\"}]," +
                        "\"pagination\":{\"page\":1,\"page_size\":50,\"items\":1,\"total\":1}}" +
                        "}";
                MockServer.writeJson(ex, 200, json);
            });

            TrafficmindClient client = TrafficmindClient.builder().email("e").apiKey("k").baseUrl(server.baseUri()).allowInsecureTransportForTesting(true).build();
            ApiResponse<List<DomainDNSRecord>> res = client.dns().listWithInfo(null, "z1");

            assertEquals(1, res.getResult().size());
            assertNotNull(res.getResultInfo());
            assertEquals(1, res.getResultInfo().getPage());
            assertEquals(50, res.getResultInfo().getPageSize());
            assertEquals(1, res.getResultInfo().getTotalCount());
            assertNotNull(res.getMeta());
            assertEquals("req-dns-1", res.getMeta().getRequestId());
            assertEquals("2026-03-13T10:20:33Z", res.getMeta().getTimestamp());
            assertNotNull(res.getStatus());
            assertEquals("ok", res.getStatus().getCode());
            assertEquals("dns list loaded", res.getStatus().getMessage());
        }
    }

    @Test
    void batchActionSerializesBodyAndParsesResponse() throws Exception {
        try (MockServer server = new MockServer()) {
            server.when("POST", "/public/v1/domains/z1/records/batch", ex -> {
                String body = MockServer.readBody(ex);
                assertTrue(body.contains("\"creates\""), "body: " + body);
                assertTrue(body.contains("\"type\":\"A\""), "body: " + body);
                assertTrue(body.contains("\"name\":\"b.example.com\""), "body: " + body);

                String json = "{" +
                        "\"status\":{\"code\":\"created\"}," +
                        "\"payload\":{\"batch\":{\"creates\":[{\"id\":\"r2\",\"type\":\"A\",\"name\":\"b.example.com\",\"content\":\"2.2.2.2\"}]}}" +
                        "}";
                MockServer.writeJson(ex, 201, json);
            });

            TrafficmindClient client = TrafficmindClient.builder().email("e").apiKey("k").baseUrl(server.baseUri()).allowInsecureTransportForTesting(true).build();

            BatchDNSCreate create = new BatchDNSCreate();
            create.setType("A");
            create.setName("b.example.com");
            create.setContent("2.2.2.2");

            BatchDNSRequest req = new BatchDNSRequest();
            req.setCreates(List.of(create));

            ApiResponse<ResponseDnsRecordsBatchesResult> result = client.dns().batchAction(req, "z1");
            assertNotNull(result);
            assertNotNull(result.getResult().getCreates());
            assertEquals(1, result.getResult().getCreates().size());
            assertEquals("r2", result.getResult().getCreates().get(0).getId());
        }
    }

    @Test
    void listAllWalksThroughPages() throws Exception {
        try (MockServer server = new MockServer()) {
            server.when("GET", "/public/v1/domains/z1/records", ex -> {
                String qs = ex.getRequestURI().getRawQuery();
                if (qs != null && qs.contains("page=1")) {
                    MockServer.writeJson(ex, 200, "{\"status\":{\"code\":\"ok\"},\"payload\":{\"items\":[{\"id\":\"r1\"}],\"pagination\":{\"page\":1,\"page_size\":1,\"total\":2,\"items\":1}}}");
                } else {
                    MockServer.writeJson(ex, 200, "{\"status\":{\"code\":\"ok\"},\"payload\":{\"items\":[{\"id\":\"r2\"}],\"pagination\":{\"page\":2,\"page_size\":1,\"total\":2,\"items\":1}}}");
                }
            });

            TrafficmindClient client = TrafficmindClient.builder().email("e").apiKey("k").baseUrl(server.baseUri()).allowInsecureTransportForTesting(true).build();
            assertEquals(2, client.dns().listAll(DnsListRequest.create().pageSize(1), "z1").getResult().size());
        }
    }

    @Test
    void listAllHonorsMaxPagesLimit() throws Exception {
        try (MockServer server = new MockServer()) {
            server.when("GET", "/public/v1/domains/z1/records", ex -> {
                String qs = ex.getRequestURI().getRawQuery();
                if (qs != null && qs.contains("page=1")) {
                    MockServer.writeJson(ex, 200, "{\"status\":{\"code\":\"ok\"},\"payload\":{\"items\":[{\"id\":\"r1\"}],\"pagination\":{\"page\":1,\"page_size\":1,\"total\":3,\"items\":1}}}");
                } else if (qs != null && qs.contains("page=2")) {
                    MockServer.writeJson(ex, 200, "{\"status\":{\"code\":\"ok\"},\"payload\":{\"items\":[{\"id\":\"r2\"}],\"pagination\":{\"page\":2,\"page_size\":1,\"total\":3,\"items\":1}}}");
                } else {
                    MockServer.writeJson(ex, 200, "{\"status\":{\"code\":\"ok\"},\"payload\":{\"items\":[{\"id\":\"r3\"}],\"pagination\":{\"page\":3,\"page_size\":1,\"total\":3,\"items\":1}}}");
                }
            });

            TrafficmindClient client = TrafficmindClient.builder().email("e").apiKey("k").baseUrl(server.baseUri()).allowInsecureTransportForTesting(true).build();
            assertEquals(2, client.dns().listAll(DnsListRequest.create().pageSize(1), "z1", 2).getResult().size());
        }
    }

    @Test
    void listAllStopsWhenThreadInterrupted() throws Exception {
        try (MockServer server = new MockServer()) {
            server.when("GET", "/public/v1/domains/z1/records", ex ->
                    MockServer.writeJson(ex, 200, "{\"status\":{\"code\":\"ok\"},\"payload\":{\"items\":[],\"pagination\":{\"page\":1,\"page_size\":1,\"total\":2,\"items\":0}}}"));

            TrafficmindClient client = TrafficmindClient.builder().email("e").apiKey("k").baseUrl(server.baseUri()).allowInsecureTransportForTesting(true).build();
            Thread.currentThread().interrupt();
            try {
                assertThrows(IllegalStateException.class, () -> client.dns().listAll(DnsListRequest.create(), "z1", 2));
            } finally {
                Thread.interrupted();
            }
        }
    }
}
