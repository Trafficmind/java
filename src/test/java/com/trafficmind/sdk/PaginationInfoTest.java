package com.trafficmind.sdk;

import com.trafficmind.sdk.dto.ApiResponse;
import com.trafficmind.sdk.dto.ResponseDomainRecord;
import com.trafficmind.sdk.dto.ResultInfo;
import com.trafficmind.sdk.dto.ResponseMeta;
import com.trafficmind.sdk.request.DomainListRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class PaginationInfoTest {

    @Test
    void listWithInfoReturnsResultInfo() throws Exception {
        try (MockServer server = new MockServer()) {
            server.when("GET", "/public/v1/domains", ex -> {
                String json = "{" +
                        "\"status\":{\"code\":\"ok\",\"message\":\"success\"}," +
                        "\"meta\":{\"request_id\":\"req-1\",\"timestamp\":\"2026-03-13T10:20:33Z\"}," +
                        "\"payload\":{\"items\":[{\"id\":\"z1\",\"name\":\"example.com\"}]," +
                        "\"pagination\":{\"page\":2,\"page_size\":50,\"items\":1,\"total\":123}}" +
                        "}";
                MockServer.writeJson(ex, 200, json);
            });

            TrafficmindClient client = TrafficmindClient.builder().email("e").apiKey("k").baseUrl(server.baseUri()).allowInsecureTransportForTesting(true).build();
            ApiResponse<java.util.List<ResponseDomainRecord>> res = client.domains().listWithInfo(DomainListRequest.create().page(2).pageSize(50));

            assertEquals(1, res.getResult().size());
            assertEquals("z1", res.getResult().get(0).getId());

            ResultInfo info = res.getResultInfo();
            assertNotNull(info);
            assertEquals(2, info.getPage());
            assertEquals(50, info.getPageSize());
            assertEquals(1, info.getItems());
            assertEquals(123, info.getTotalCount());

            ResponseMeta meta = res.getMeta();
            assertNotNull(meta);
            assertEquals("req-1", meta.getRequestId());
            assertEquals("2026-03-13T10:20:33Z", meta.getTimestamp());

            assertNotNull(res.getStatus());
            assertEquals("ok", res.getStatus().getCode());
            assertEquals("success", res.getStatus().getMessage());
        }
    }
}
