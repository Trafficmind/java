package com.trafficmind.sdk;

import com.trafficmind.sdk.dto.ApiResponse;
import com.trafficmind.sdk.dto.ResponseDomainSetting;
import com.trafficmind.sdk.dto.UpdateDomainSettingRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class DomainSettingsApiTest {

    @Test
    void getAndUpdateDomainSettingWork() throws Exception {
        try (MockServer server = new MockServer()) {
            server.when("GET", "/public/v1/domains/z1/settings/s1", ex -> {
                MockServer.writeJson(ex, 200,
                        "{\"status\":{\"code\":\"ok\"},\"payload\":{\"setting\":{\"id\":\"s1\",\"value\":\"on\"}}}"
                );
            });

            server.when("PATCH", "/public/v1/domains/z1/settings/s1", ex -> {
                String body = MockServer.readBody(ex);
                assertTrue(body.contains("\"value\":\"off\""), "body: " + body);
                MockServer.writeJson(ex, 200,
                        "{\"status\":{\"code\":\"ok\"},\"payload\":{\"setting\":{\"id\":\"s1\",\"value\":\"off\"}}}"
                );
            });

            TrafficmindClient client = TrafficmindClient.builder().email("e").apiKey("k").baseUrl(server.baseUri()).allowInsecureTransportForTesting(true).build();

            ApiResponse<ResponseDomainSetting> got = client.domainSettings().get("z1", "s1");
            assertEquals("s1", got.getResult().getId());
            assertEquals("on", got.getResult().getValue());

            UpdateDomainSettingRequest req = new UpdateDomainSettingRequest();
            req.setValue("off");

            ApiResponse<ResponseDomainSetting> updated = client.domainSettings().update("z1", "s1", req);
            assertEquals("off", updated.getResult().getValue());
        }
    }
}
