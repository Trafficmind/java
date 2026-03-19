package com.trafficmind.sdk;

import com.trafficmind.sdk.dto.CDNStorageResponse;
import com.trafficmind.sdk.dto.CDNUserResponse;
import com.trafficmind.sdk.dto.ApiResponse;
import com.trafficmind.sdk.dto.CreateCDNStorageRequest;
import com.trafficmind.sdk.dto.ResultInfo;
import com.trafficmind.sdk.dto.ResponseMeta;
import com.trafficmind.sdk.dto.SuccessResponse;
import com.trafficmind.sdk.dto.SyncStatus;
import com.trafficmind.sdk.request.CdnStorageListRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

final class CdnApiTest {

    @Test
    void cdnOperationsUseCorrectMethodsAndParseResponses() throws Exception {
        try (MockServer server = new MockServer()) {
            server.when("GET", "/public/v1/cdn/storage", ex -> {
                assertEquals("page=2&page_size=10", ex.getRequestURI().getQuery());
                MockServer.writeJson(ex, 200,
                        "{\"status\":{\"code\":\"ok\"},\"meta\":{\"request_id\":\"req-cdn-1\",\"timestamp\":\"2026-03-13T10:20:33Z\"},\"payload\":{\"items\":[{\"id\":\"s0\",\"name\":\"store0\"}],\"pagination\":{\"page\":2,\"page_size\":10,\"items\":1,\"total\":7}}}"
                );
            });

            server.when("POST", "/public/v1/cdn/storage", ex -> {
                String body = MockServer.readBody(ex);
                assertTrue(body.contains("\"name\":\"store1\""), "body: " + body);
                assertFalse(body.contains("\"domain_id\""), "body: " + body);
                MockServer.writeJson(ex, 201,
                        "{\"status\":{\"code\":\"created\"},\"payload\":{\"storage\":{\"id\":\"s1\",\"name\":\"store1\"}}}"
                );
            });

            server.when("DELETE", "/public/v1/cdn/storage/s1", ex -> {
                MockServer.writeJson(ex, 200, "{\"status\":{\"code\":\"ok\"},\"payload\":{\"result\":{\"message\":\"deleted\"}}}");
            });

            server.when("POST", "/public/v1/cdn/storage/s1/refresh", ex -> {
                String body = MockServer.readBody(ex).trim();
                assertEquals("{}", body.isEmpty() ? "{}" : body);
                MockServer.writeJson(ex, 200, "{\"status\":{\"code\":\"ok\"},\"payload\":{\"sync\":{\"status\":\"queued\"}}}");
            });

            server.when("GET", "/public/v1/cdn/storage/s1/user", ex -> {
                MockServer.writeJson(ex, 200,
                        "{\"status\":{\"code\":\"ok\"},\"payload\":{\"user\":{\"username\":\"u1\",\"password\":\"p1\",\"storage_id\":\"s1\"}}}"
                );
            });

            server.when("POST", "/public/v1/cdn/user", ex -> {
                String body = MockServer.readBody(ex);
                assertTrue(body.contains("\"storage_id\":\"s1\""), "body: " + body);
                MockServer.writeJson(ex, 201,
                        "{\"status\":{\"code\":\"created\"},\"payload\":{\"user\":{\"username\":\"u1\",\"password\":\"p1\",\"storage_id\":\"s1\"}}}"
                );
            });

            server.when("POST", "/public/v1/cdn/user/u1/revoke", ex -> {
                String body = MockServer.readBody(ex).trim();
                assertEquals("{}", body.isEmpty() ? "{}" : body);
                MockServer.writeJson(ex, 200,
                        "{\"status\":{\"code\":\"ok\"},\"payload\":{\"user\":{\"username\":\"u1\",\"password\":\"p2\",\"storage_id\":\"s1\"}}}"
                );
            });

            TrafficmindClient client = TrafficmindClient.builder().email("e").apiKey("k").baseUrl(server.baseUri()).allowInsecureTransportForTesting(true).build();

            CreateCDNStorageRequest req = new CreateCDNStorageRequest();
            req.setName("store1");

            ApiResponse<List<CDNStorageResponse>> storages = client.cdn().listStorages(CdnStorageListRequest.create().page(2).pageSize(10));
            assertEquals(1, storages.getResult().size());
            assertEquals("s0", storages.getResult().get(0).getId());

            ApiResponse<List<CDNStorageResponse>> pagedStorages = client.cdn().listStoragesWithInfo(CdnStorageListRequest.create().page(2).pageSize(10));
            assertEquals(1, pagedStorages.getResult().size());
            ResultInfo storagesInfo = pagedStorages.getResultInfo();
            assertNotNull(storagesInfo);
            assertEquals(2, storagesInfo.getPage());
            assertEquals(10, storagesInfo.getPageSize());
            assertEquals(1, storagesInfo.getItems());
            assertEquals(7, storagesInfo.getTotalCount());
            ResponseMeta storagesMeta = pagedStorages.getMeta();
            assertNotNull(storagesMeta);
            assertEquals("req-cdn-1", storagesMeta.getRequestId());

            ApiResponse<CDNStorageResponse> storage = client.cdn().createStorage(req);
            assertEquals("s1", storage.getResult().getId());

            ApiResponse<SyncStatus> syncStatus = client.cdn().refreshStorage("s1");
            assertEquals("queued", syncStatus.getResult().getStatus());

            ApiResponse<CDNUserResponse> creds = client.cdn().getSftpCredentials("s1");
            assertEquals("u1", creds.getResult().getUsername());
            assertEquals("p1", creds.getResult().getPassword());

            ApiResponse<CDNUserResponse> created = client.cdn().createSftpUser("s1");
            assertEquals("u1", created.getResult().getUsername());

            ApiResponse<CDNUserResponse> revoked = client.cdn().revokeSftpUser("u1");
            assertEquals("u1", revoked.getResult().getUsername());
            assertEquals("p2", revoked.getResult().getPassword());

            ApiResponse<SuccessResponse> deleted = client.cdn().deleteStorage("s1");
            assertEquals("deleted", deleted.getResult().getMessage());
        }
    }

    @Test
    void sendsIdempotencyKeyForPostRequestsWhenConfigured() throws Exception {
        try (MockServer server = new MockServer()) {
            server.when("POST", "/public/v1/cdn/storage", ex -> {
                MockServer.assertHeader(ex, "X-Idempotency-Key", "idem-123");
                MockServer.writeJson(ex, 201, "{\"status\":{\"code\":\"created\"},\"payload\":{\"storage\":{\"id\":\"s1\"}}}");
            });

            TrafficmindClient client = TrafficmindClient.builder()
                    .email("e")
                    .apiKey("k")
                    .baseUrl(server.baseUri())
                    .allowInsecureTransportForTesting(true)
                    .withIdempotencyKey("idem-123")
                    .build();

            CreateCDNStorageRequest req = new CreateCDNStorageRequest();
            req.setName("store1");
            assertEquals("s1", client.cdn().createStorage(req).getResult().getId());
        }
    }
}
